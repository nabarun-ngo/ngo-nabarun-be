import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { ListBooksQuery } from './list-books.query';
import { IBookRepository } from '../../../domain/repositories/book.repository';
import { BookMapper } from '../../mappers/book.mapper';
import { BookDetailDto } from '../../dtos/book.dto';

@QueryHandler(ListBooksQuery)
@Injectable()
export class ListBooksHandler implements IQueryHandler<ListBooksQuery, PagedResponse<BookDetailDto>> {
  constructor(@Inject(IBookRepository) private readonly repo: IBookRepository) {}

  async execute(query: ListBooksQuery): Promise<PagedResponse<BookDetailDto>> {
    const filter = new BaseFilter(query.filter, query.filter.pageIndex ?? 0, query.filter.pageSize ?? 20);
    const page = await this.repo.findPaged({
      pageIndex: filter.pageIndex,
      pageSize: filter.pageSize,
      props: filter.props,
    });
    return {
      content: page.content.map(BookMapper.toDto),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
