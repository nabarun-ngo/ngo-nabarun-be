import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter } from '@nabarun-ngo/nestjs-shared-core';
import { ListBooksQuery } from './list-books.query';
import { IBookRepository } from '../../../domain/repositories/book.repository';
import { BookMapper } from '../../mappers/book.mapper';
import { BookListResponseDto } from '../../dtos/book.dto';

@QueryHandler(ListBooksQuery)
@Injectable()
export class ListBooksHandler implements IQueryHandler<ListBooksQuery, BookListResponseDto> {
  constructor(@Inject(IBookRepository) private readonly repo: IBookRepository) {}

  async execute(query: ListBooksQuery): Promise<BookListResponseDto> {
    const filter = new BaseFilter(query.filter, query.pageIndex ?? 0, query.pageSize ?? 20);
    const page = await this.repo.findPaged({
      pageIndex: filter.pageIndex,
      pageSize: filter.pageSize,
      props: filter.props,
    });
    return {
      items: page.content.map(BookMapper.toDto),
      total: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
