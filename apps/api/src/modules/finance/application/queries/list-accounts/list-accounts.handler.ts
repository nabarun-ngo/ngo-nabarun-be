import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter } from '@nabarun-ngo/nestjs-shared-core';
import { IAccountRepository } from '../../../domain/repositories/account.repository';
import { AccountMapper } from '../../mappers/account.mapper';
import { ListAccountsQuery } from './list-accounts.query';
import { PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { AccountDetailDto } from '../../dtos/account.dto';

@QueryHandler(ListAccountsQuery)
@Injectable()
export class ListAccountsHandler implements IQueryHandler<ListAccountsQuery, PagedResponse<AccountDetailDto>> {
  constructor(@Inject(IAccountRepository) private readonly repo: IAccountRepository) { }

  async execute(query: ListAccountsQuery): Promise<PagedResponse<AccountDetailDto>> {
    const filter = new BaseFilter(query.filter, query.filter?.pageIndex ?? 0, query.filter?.pageSize ?? 20);
    const page = await this.repo.findPaged({
      pageIndex: filter.pageIndex,
      pageSize: filter.pageSize,
      props: {
        accountHolderId: query.userId ?? filter.props?.accountHolderId,
        id: filter.props?.accountId,
        status: filter.props?.status,
        type: filter.props?.type,
        ownerType: filter.props?.ownerType,
        includeBalance: filter.props?.includeBalance === 'Y',
      },
    });
    const includePayment = filter.props?.includePaymentDetail === 'Y';
    return {
      content: page.content.map((a) =>
        AccountMapper.toDto(a, { includeBankDetail: includePayment, includeUpiDetail: includePayment }),
      ),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}

