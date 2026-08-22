import { Inject, Injectable } from '@nestjs/common';
import { CommandBus, CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { Earning } from '../../../domain/aggregates/earning/earning.aggregate';
import { AccountStatus } from '../../../domain/enums/account-status.enum';
import { AccountType } from '../../../domain/enums/account-type.enum';
import { EarningCategory, EarningStatus } from '../../../domain/enums/earning.enum';
import { TransactionRefType, TransactionType } from '../../../domain/enums/transaction.enum';
import { IAccountRepository } from '../../../domain/repositories/account.repository';
import { IEarningRepository } from '../../../domain/repositories/earning.repository';
import { CreateTransactionCommand } from '../create-transaction/create-transaction.command';
import { UpdateEarningCommand } from './update-earning.command';

@CommandHandler(UpdateEarningCommand)
@Injectable()
export class UpdateEarningHandler implements ICommandHandler<UpdateEarningCommand, Earning> {
  constructor(
    @Inject(IEarningRepository) private readonly earningRepository: IEarningRepository,
    @Inject(IAccountRepository) private readonly accountRepository: IAccountRepository,
    private readonly commandBus: CommandBus,
  ) { }

  async execute({ params: request }: UpdateEarningCommand): Promise<Earning> {
    const earning = await this.earningRepository.findById(request.id);
    if (!earning) throw new BusinessException('Earning not found with id: ' + request.id);

    earning.update({
      amount: request.amount,
      category: request.category,
      description: request.description,
      earningDate: request.earningDate,
      source: request.source,
    });

    if (request.status === EarningStatus.RECEIVED) {
      if (!request.accountId) throw new BusinessException('Account ID is required to mark earning as received');
      if (!request.earningDate) throw new BusinessException('Earning Date is required to mark earning as received');

      const receiveAccount = await this.accountRepository.findById(request.accountId);
      if (!receiveAccount) {
        throw new BusinessException('Receive account not found with id: ' + request.accountId);
      }
      if (receiveAccount.status !== AccountStatus.ACTIVE) {
        throw new BusinessException('Receive account must be active');
      }
      if (receiveAccount.type === AccountType.INVESTMENT) {
        const category = request.category ?? earning.category;
        if (category !== EarningCategory.INTEREST) {
          throw new BusinessException('Only INTEREST earnings can be received on investment accounts');
        }
      } else if (receiveAccount.type !== AccountType.BANK) {
        throw new BusinessException('Earnings can only be received on bank or investment accounts');
      }

      earning.markAsReceived(request.accountId, request.earningDate, request.userId);
      const txnRef = await this.commandBus.execute(
        new CreateTransactionCommand({
          txnAmount: earning.amount,
          currency: earning.currency,
          txnDescription: `Earning - ${earning.category} - ${earning.description}`,
          txnRefId: earning.id,
          txnRefType: TransactionRefType.EARNING,
          accountId: request.accountId,
          txnDate: earning.earningDate,
          txnType: TransactionType.IN,
        }),
      );
      earning.setTransactionId(txnRef);
    }
    if (request.status === EarningStatus.CANCELLED) earning.cancel();

    return this.earningRepository.update(request.id, earning);
  }
}
