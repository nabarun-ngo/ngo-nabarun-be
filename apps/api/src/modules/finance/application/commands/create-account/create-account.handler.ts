import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, EventBus, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException, generateUniqueNDigitNumber, IUserLookupPort } from '@nabarun-ngo/nestjs-shared-core';
import { ILockingPort } from '@nabarun-ngo/nestjs-shared-persistence';
import { Account } from '../../../domain/aggregates/account/account.aggregate';
import { ORG_ACCOUNT_DISPLAY_NAME } from '../../../domain/config/account-type.config';
import { AccountOwnerType } from '../../../domain/enums/account-owner-type.enum';
import { AccountStatus } from '../../../domain/enums/account-status.enum';
import { AccountType } from '../../../domain/enums/account-type.enum';
import { TransactionRefType } from '../../../domain/enums/transaction.enum';
import { IAccountRepository } from '../../../domain/repositories/account.repository';
import { BankDetail } from '../../../domain/value-objects/bank-detail.vo';
import { UPIDetail } from '../../../domain/value-objects/upi-detail.vo';
import {
  assertCreateAccountDetails,
  assertUpdateBankDetailIfsc,
} from '../../../domain/validation/account-detail.validation';
import { AccountBankIfscValidationService } from '../../services/account-bank-ifsc-validation.service';
import { CreateAccountCommand } from './create-account.command';

@CommandHandler(CreateAccountCommand)
@Injectable()
export class CreateAccountHandler implements ICommandHandler<CreateAccountCommand, Account> {
  constructor(
    @Inject(IAccountRepository) private readonly accountRepository: IAccountRepository,
    @Inject(IUserLookupPort) private readonly userLookup: IUserLookupPort,
    @Inject(ILockingPort) private readonly lockingService: ILockingPort,
    private readonly eventBus: EventBus,
    private readonly bankIfscValidation: AccountBankIfscValidationService,
  ) { }

  async execute({ params: request }: CreateAccountCommand): Promise<Account> {
    let accountHolderId: string | undefined;
    let accountHolderName: string | undefined;
    let custodianUserIds: string[] | undefined;

    if (request.ownerType === AccountOwnerType.ORG) {
      accountHolderName = ORG_ACCOUNT_DISPLAY_NAME;
      const requestedCustodianIds = this.normalizeCustodianUserIds(request);
      if (requestedCustodianIds.length) {
        custodianUserIds = await this.resolveCustodians(requestedCustodianIds);
      }
    } else {
      if (!request.accountHolderId) {
        throw new BusinessException('Account holder is required for individual accounts');
      }
      const user = await this.userLookup.findById(request.accountHolderId);
      if (!user) throw new BusinessException('User not found with id ' + request.accountHolderId);
      accountHolderId = request.accountHolderId;
      accountHolderName =
        user.fullName ??
        ([user.firstName, user.lastName].filter(Boolean).join(' ').trim() || user.email);
    }

    const existing = await this.accountRepository.findAll({
      status: [AccountStatus.ACTIVE],
      type: [request.type],
      ownerType: [request.ownerType],
      accountHolderId: request.ownerType === AccountOwnerType.ORG ? null : accountHolderId,
    });
    if (existing.length > 0) {
      throw new BusinessException(
        'An active account of this type already exists' +
        (request.ownerType === AccountOwnerType.INDIVIDUAL ? ' for this account holder' : ''),
      );
    }

    try {
      assertCreateAccountDetails(request.type, request.bankDetail, {
        upiDetailsCount: request.upiDetails?.length ?? 0,
        description: request.description,
      });
      await assertUpdateBankDetailIfsc(
        request.type,
        request.bankDetail,
        bank => this.bankIfscValidation.assertBankDetailIfscIntegrity(bank),
      );
    } catch (error) {
      throw new BusinessException(error instanceof Error ? error.message : 'Invalid account details');
    }

    const upiDetails = request.upiDetails?.length
      ? request.upiDetails.map((detail) => new UPIDetail(
        detail.id,
        detail.payeeName,
        detail.upiId,
        detail.mobileNumber,
        detail.qrData,
        detail.label,
        detail.isPrimary,
      ))
      : undefined;

    const bankDetail = request.bankDetail ? this.toBankDetail(request.bankDetail) : undefined;

    if (request.type === AccountType.INVESTMENT) {
      return this.createAndFundInvestment({
        request,
        accountHolderId,
        accountHolderName,
        custodianUserIds,
        bankDetail: bankDetail!,
        upiDetails,
      });
    }

    const account = Account.create({
      name: request.name,
      type: request.type,
      ownerType: request.ownerType,
      currency: request.currency,
      description: request.description,
      accountHolderId,
      accountHolderName,
      custodianUserIds,
      bankDetail,
      upiDetails,
    });

    const saved = await this.accountRepository.create(account);
    this.publishEvents(saved);
    return saved;
  }

  private async createAndFundInvestment(deps: {
    request: CreateAccountCommand['params'];
    accountHolderId?: string;
    accountHolderName?: string;
    custodianUserIds?: string[];
    bankDetail: BankDetail;
    upiDetails?: UPIDetail[];
  }): Promise<Account> {
    const sourceAccountId = deps.bankDetail.sourceAccountId!;
    const investmentAmount = deps.bankDetail.investmentAmount!;

    return this.lockingService.withLocks([sourceAccountId], async () => {
      const source = await this.accountRepository.findById(sourceAccountId);
      if (!source) {
        throw new BusinessException('Source bank account not found');
      }
      if (source.type !== AccountType.BANK) {
        throw new BusinessException('Source account must be a bank account');
      }
      if (source.status !== AccountStatus.ACTIVE) {
        throw new BusinessException('Source bank account must be active');
      }
      if (!source.hasSufficientFunds(investmentAmount)) {
        throw new BusinessException('Source bank account has insufficient balance');
      }

      const investment = Account.create({
        name: deps.request.name,
        type: AccountType.INVESTMENT,
        ownerType: deps.request.ownerType,
        currency: deps.request.currency,
        description: deps.request.description,
        accountHolderId: deps.accountHolderId,
        accountHolderName: deps.accountHolderName,
        custodianUserIds: deps.custodianUserIds,
        bankDetail: deps.bankDetail,
        upiDetails: deps.upiDetails,
      });

      const savedInvestment = await this.accountRepository.create(investment);
      const fundedInvestment = await this.accountRepository.findById(savedInvestment.id);
      if (!fundedInvestment) {
        throw new BusinessException('Failed to load created investment account');
      }

      const transactionRef = `TXR${generateUniqueNDigitNumber(10)}`;
      const txnDate = new Date();
      const description = `Investment funding ${investmentAmount}`;

      source.debit(investmentAmount, {
        transactionRef,
        description,
        txnDate,
        referenceType: TransactionRefType.NONE,
        refAccountId: fundedInvestment.id,
      });
      fundedInvestment.credit(investmentAmount, {
        transactionRef,
        description,
        txnDate,
        referenceType: TransactionRefType.NONE,
        refAccountId: source.id,
      });

      await this.accountRepository.update(source.id, source);
      const updatedInvestment = await this.accountRepository.update(
        fundedInvestment.id,
        fundedInvestment,
      );

      this.publishEvents(savedInvestment, source, fundedInvestment);
      return updatedInvestment;
    });
  }

  private toBankDetail(input: NonNullable<CreateAccountCommand['params']['bankDetail']>): BankDetail {
    return new BankDetail(
      input.bankAccountHolderName,
      input.bankName,
      input.bankBranch,
      input.bankAccountNumber,
      input.bankAccountType,
      input.IFSCNumber,
      input.maturityDate,
      input.maturityAmount,
      input.investmentAmount,
      input.sourceAccountId,
      input.dematId,
      input.interestRate,
      input.interestPayingTerm,
    );
  }

  private normalizeCustodianUserIds(request: CreateAccountCommand['params']): string[] {
    const ids = request.custodianUserIds?.length
      ? request.custodianUserIds
      : request.custodianUserId
        ? [request.custodianUserId]
        : [];
    return [...new Set(ids.map((id) => id.trim()).filter(Boolean))];
  }

  private async resolveCustodians(ids: string[]): Promise<string[]> {
    for (const id of ids) {
      const custodian = await this.userLookup.findById(id);
      if (!custodian) throw new BusinessException('Custodian not found with id ' + id);
    }
    return ids;
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
