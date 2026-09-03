import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { IExpenseRepository } from '../../../domain/repositories/expense.repository';
import { ExpenseMapper } from '../../mappers/expense.mapper';
import { ListExpensesQuery } from './list-expenses.query';
import { ExpenseDetailDto } from '../../dtos/expense.dto';

@QueryHandler(ListExpensesQuery)
@Injectable()
export class ListExpensesHandler implements IQueryHandler<ListExpensesQuery, PagedResponse<ExpenseDetailDto>> {
  constructor(@Inject(IExpenseRepository) private readonly repo: IExpenseRepository) { }

  async execute(query: ListExpensesQuery): Promise<PagedResponse<ExpenseDetailDto>> {
    const filter = new BaseFilter(query.filter, query.filter.pageIndex ?? 0, query.filter.pageSize ?? 20);
    const page = await this.repo.findPaged({
      pageIndex: filter.pageIndex,
      pageSize: filter.pageSize,
      props: {
        expenseId: filter.props?.expenseId,
        expenseRefId: filter.props?.expenseRefId,
        expenseStatus: filter.props?.expenseStatus,
        payerId: filter.props?.payerId,
        startDate: filter.props?.startDate,
        endDate: filter.props?.endDate,
      },
    });
    return {
      content: page.content.map(ExpenseMapper.toDto),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}

