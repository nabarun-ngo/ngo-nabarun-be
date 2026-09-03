import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter, BusinessException, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { IAccountRepository } from '../../../domain/repositories/account.repository';
import { ITransactionRepository } from '../../../domain/repositories/transaction.repository';
import { TransactionMapper } from '../../mappers/transaction.mapper';
import { ListAccountTransactionsQuery } from './list-account-transactions.query';
import { TransactionDetailDto } from '../../dtos/transaction.dto';

@QueryHandler(ListAccountTransactionsQuery)
@Injectable()
export class ListAccountTransactionsHandler implements IQueryHandler<ListAccountTransactionsQuery, PagedResponse<TransactionDetailDto>> {
  constructor(
    @Inject(IAccountRepository) private readonly accountRepo: IAccountRepository,
    @Inject(ITransactionRepository) private readonly txnRepo: ITransactionRepository,
  ) { }

  async execute(query: ListAccountTransactionsQuery): Promise<PagedResponse<TransactionDetailDto>> {
    const account = await this.accountRepo.findById(query.accountId);
    if (query.userId && account?.accountHolderId !== query.userId) {
      throw new BusinessException('Account does not belongs to user.');
    }
    const filter = new BaseFilter(query.filter, query.filter?.pageIndex ?? 0, query.filter?.pageSize ?? 20);
    const page = await this.txnRepo.findPaged({
      pageIndex: filter.pageIndex,
      pageSize: filter.pageSize,
      props: {
        accountIds: [query.accountId],
        transactionRef: filter.props?.transactionRef,
        type: filter.props?.txnType,
        status: filter.props?.txnStatus,
        startDate: filter.props?.startDate,
        endDate: filter.props?.endDate,
        id: filter.props?.txnId,
      },
    });
    return {
      content: page.content.map(TransactionMapper.toDto),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}

