import { Inject, Injectable } from '@nestjs/common';
import { CommandBus, CommandHandler, EventBus, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { Earning } from '../../../domain/aggregates/earning/earning.aggregate';
import { AccountStatus } from '../../../domain/enums/account-status.enum';
import { AccountType } from '../../../domain/enums/account-type.enum';
import { EarningCategory } from '../../../domain/enums/earning.enum';
import { TransactionRefType, TransactionType } from '../../../domain/enums/transaction.enum';
import { IAccountRepository } from '../../../domain/repositories/account.repository';
import { IEarningRepository } from '../../../domain/repositories/earning.repository';
import { CreateTransactionCommand } from '../create-transaction/create-transaction.command';
import { CreateEarningCommand } from './create-earning.command';

@CommandHandler(CreateEarningCommand)
@Injectable()
export class CreateEarningHandler implements ICommandHandler<CreateEarningCommand, Earning> {
  constructor(
    @Inject(IEarningRepository) private readonly earningRepository: IEarningRepository,
    @Inject(IAccountRepository) private readonly accountRepository: IAccountRepository,
    private readonly commandBus: CommandBus,
    private readonly eventBus: EventBus,
  ) {}

  async execute({ params: request }: CreateEarningCommand): Promise<Earning> {
    if (request.category !== EarningCategory.INTEREST && request.accountId) {
      throw new BusinessException('Account can only be selected for INTEREST earnings');
    }

    const earningDate = request.category === EarningCategory.INTEREST
      ? new Date()
      : undefined;

    if (request.category === EarningCategory.INTEREST) {
      if (!request.accountId) {
        throw new BusinessException('Account ID is required for INTEREST earnings');
      }
      const receiveAccount = await this.accountRepository.findById(request.accountId);
      if (!receiveAccount) {
        throw new BusinessException('Receive account not found with id: ' + request.accountId);
      }
      if (receiveAccount.status !== AccountStatus.ACTIVE) {
        throw new BusinessException('Receive account must be active');
      }
      if (
        receiveAccount.type !== AccountType.BANK
        && receiveAccount.type !== AccountType.INVESTMENT
      ) {
        throw new BusinessException(
          'Interest earnings can only be received on bank or investment accounts',
        );
      }
    }

    const earning = Earning.create({
      category: request.category,
      amount: request.amount,
      currency: request.currency,
      source: request.source,
      description: request.description ?? '',
      referenceId: request.accountId,
      referenceType: request.accountId ? 'ACCOUNT' : undefined,
      earningDate,
      createdById: request.userId,
    });

    if (request.category === EarningCategory.INTEREST && request.accountId && earningDate) {
      earning.markAsReceived(request.accountId, earningDate, request.userId);
      const transactionId = await this.commandBus.execute<CreateTransactionCommand, string>(
        new CreateTransactionCommand({
          txnAmount: earning.amount,
          currency: earning.currency,
          txnDescription: `Earning - ${earning.category} - ${earning.description}`,
          txnRefId: earning.id,
          txnRefType: TransactionRefType.EARNING,
          accountId: request.accountId,
          txnDate: earningDate,
          txnType: TransactionType.IN,
        }),
      );
      earning.setTransactionId(transactionId);
    }

    const saved = await this.earningRepository.create(earning);
    const events = [...earning.domainEvents];
    earning.clearEvents();
    this.eventBus.publishAll(events);
    return saved;
  }
}

