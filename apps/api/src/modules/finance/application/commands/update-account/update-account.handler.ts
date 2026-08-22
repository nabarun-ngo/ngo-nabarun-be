import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, EventBus, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException, generateUniqueNDigitNumber } from '@nabarun-ngo/nestjs-shared-core';
import { ILockingPort } from '@nabarun-ngo/nestjs-shared-persistence';
import { Account } from '../../../domain/aggregates/account/account.aggregate';
import { AccountStatus } from '../../../domain/enums/account-status.enum';
import { AccountType } from '../../../domain/enums/account-type.enum';
import { EarningCategory, EarningStatus } from '../../../domain/enums/earning.enum';
import { TransactionRefType } from '../../../domain/enums/transaction.enum';
import { IAccountRepository } from '../../../domain/repositories/account.repository';
import { IEarningRepository } from '../../../domain/repositories/earning.repository';
import { BankDetail } from '../../../domain/value-objects/bank-detail.vo';
import { UPIDetail, upiDetailFromLegacy } from '../../../domain/value-objects/upi-detail.vo';
import {
  assertBalanceNearEstimatedMaturity,
  assertUpdateAccountDetails,
  assertUpdateBankDetailIfsc,
} from '../../../domain/validation/account-detail.validation';
import { AccountBankIfscValidationService } from '../../services/account-bank-ifsc-validation.service';
import { UpdateAccountCommand } from './update-account.command';

@CommandHandler(UpdateAccountCommand)
@Injectable()
export class UpdateAccountHandler implements ICommandHandler<UpdateAccountCommand, Account> {
  constructor(
    @Inject(IAccountRepository) private readonly accountRepository: IAccountRepository,
    @Inject(IEarningRepository) private readonly earningRepository: IEarningRepository,
    @Inject(ILockingPort) private readonly lockingService: ILockingPort,
    private readonly eventBus: EventBus,
    private readonly bankIfscValidation: AccountBankIfscValidationService,
  ) { }

  async execute({ params: request }: UpdateAccountCommand): Promise<Account> {
    const account = await this.accountRepository.findById(request.id);
    if (!account) throw new BusinessException('Account not found with id: ' + request.id);

    if (request.actorUserId && account.accountHolderId !== request.actorUserId) {
      throw new BusinessException('Account does not belongs to user.');
    }

    const upiDetailsCount = request.upiDetails?.length
      ?? (request.upiDetail ? 1 : 0);

    try {
      assertUpdateAccountDetails(account.type, request.bankDetail, {
        upiDetailsCount,
        description: request.description,
        existingBankDetail: account.bankDetail,
      });
      await assertUpdateBankDetailIfsc(
        account.type,
        request.bankDetail,
        bank => this.bankIfscValidation.assertBankDetailIfscIntegrity(bank),
      );
    } catch (error) {
      throw new BusinessException(error instanceof Error ? error.message : 'Invalid bank details');
    }

    let bankDetail: BankDetail | undefined;
    if (request.bankDetail) {
      const existing = account.bankDetail;
      bankDetail = new BankDetail(
        request.bankDetail.bankAccountHolderName,
        request.bankDetail.bankName,
        request.bankDetail.bankBranch,
        request.bankDetail.bankAccountNumber,
        request.bankDetail.bankAccountType,
        request.bankDetail.IFSCNumber,
        request.bankDetail.maturityDate,
        request.bankDetail.maturityAmount,
        // Funding fields are immutable — always keep the persisted values.
        existing?.investmentAmount ?? request.bankDetail.investmentAmount,
        existing?.sourceAccountId ?? request.bankDetail.sourceAccountId,
        request.bankDetail.dematId,
        request.bankDetail.interestRate,
        request.bankDetail.interestPayingTerm,
      );
    }

    let upiDetails: UPIDetail[] | undefined;
    let replaceUpiDetails = false;
    if (request.upiDetails) {
      upiDetails = request.upiDetails.map((detail) => new UPIDetail(
        detail.id,
        detail.payeeName,
        detail.upiId,
        detail.mobileNumber,
        detail.qrData,
        detail.label,
        detail.isPrimary,
      ));
      replaceUpiDetails = true;
    } else if (request.upiDetail) {
      upiDetails = upiDetailFromLegacy(new UPIDetail(
        request.upiDetail.id,
        request.upiDetail.payeeName,
        request.upiDetail.upiId,
        request.upiDetail.mobileNumber,
        request.upiDetail.qrData,
        request.upiDetail.label,
        request.upiDetail.isPrimary ?? true,
      ));
      replaceUpiDetails = true;
    }

    account.update({
      name: request.name,
      description: request.description,
      bankDetail,
      upiDetails,
      accountHolderName: request.name,
    });

    if (request.accountStatus === 'ACTIVE') {
      account.activate();
      return this.accountRepository.update(request.id, account, { replaceUpiDetails });
    }

    if (request.accountStatus === 'CLOSED') {
      return this.closeAccount(account, { replaceUpiDetails });
    }

    return this.accountRepository.update(request.id, account, { replaceUpiDetails });
  }

  private async closeAccount(
    account: Account,
    options: { replaceUpiDetails: boolean },
  ): Promise<Account> {
    if (account.type === AccountType.BANK) {
      const linked = await this.accountRepository.findAll({
        type: [AccountType.INVESTMENT],
        status: [AccountStatus.ACTIVE],
        sourceAccountId: account.id,
        includeBalance: false,
      });
      if (linked.length) {
        throw new BusinessException(
          'Cannot close bank while active investment accounts are linked',
        );
      }
      account.close();
      return this.accountRepository.update(account.id, account, options);
    }

    if (account.type === AccountType.INVESTMENT) {
      return this.closeInvestment(account, options);
    }

    account.close();
    return this.accountRepository.update(account.id, account, options);
  }

  private async closeInvestment(
    account: Account,
    options: { replaceUpiDetails: boolean },
  ): Promise<Account> {
    const interestEarnings = await this.earningRepository.findAll({
      accountId: account.id,
      category: [EarningCategory.INTEREST],
      status: [EarningStatus.RECEIVED],
    });
    if (!interestEarnings.length) {
      throw new BusinessException(
        'Interest must be recorded via earnings before closing this investment account',
      );
    }

    const estimated = account.bankDetail?.maturityAmount;
    if (estimated != null) {
      try {
        assertBalanceNearEstimatedMaturity(account.balance, estimated);
      } catch (error) {
        throw new BusinessException(error instanceof Error ? error.message : 'Maturity proximity check failed');
      }
    }

    const sourceAccountId = account.bankDetail?.sourceAccountId;
    if (!sourceAccountId) {
      throw new BusinessException('Investment account is missing a source bank account');
    }

    const redeemAmount = account.balance;
    if (redeemAmount <= 0) {
      account.close();
      return this.accountRepository.update(account.id, account, options);
    }

    return this.lockingService.withLocks([account.id, sourceAccountId], async () => {
      const investment = await this.accountRepository.findById(account.id);
      const source = await this.accountRepository.findById(sourceAccountId);
      if (!investment) {
        throw new BusinessException('Investment account not found');
      }
      if (!source) {
        throw new BusinessException('Source bank account not found');
      }
      if (source.type !== AccountType.BANK || source.status !== AccountStatus.ACTIVE) {
        throw new BusinessException('Source bank account must be an active bank account');
      }

      const amount = investment.balance;
      if (amount > 0) {
        if (estimated != null) {
          try {
            assertBalanceNearEstimatedMaturity(amount, estimated);
          } catch (error) {
            throw new BusinessException(
              error instanceof Error ? error.message : 'Maturity proximity check failed',
            );
          }
        }

        const transactionRef = `TXR${generateUniqueNDigitNumber(10)}`;
        const txnDate = new Date();
        const description = `Investment redeem ${amount}`;

        investment.debit(amount, {
          transactionRef,
          description,
          txnDate,
          referenceType: TransactionRefType.NONE,
          refAccountId: source.id,
        });
        source.credit(amount, {
          transactionRef,
          description,
          txnDate,
          referenceType: TransactionRefType.NONE,
          refAccountId: investment.id,
        });

        await this.accountRepository.update(source.id, source);
        this.publishEvents(source);
      }

      // Re-apply non-status field updates from the outer flow onto the reloaded aggregate.
      if (account.bankDetail) {
        investment.update({
          name: account.name,
          description: account.description,
          bankDetail: account.bankDetail,
        });
      }

      investment.close();
      const closed = await this.accountRepository.update(investment.id, investment, options);
      this.publishEvents(investment);
      return closed;
    });
  }

  private publishEvents(...accounts: { domainEvents: readonly object[]; clearEvents(): void }[]): void {
    for (const account of accounts) {
      const events = [...account.domainEvents];
      account.clearEvents();
      if (events.length) {
        this.eventBus.publishAll(events as object[]);
      }
    }
  }
}
