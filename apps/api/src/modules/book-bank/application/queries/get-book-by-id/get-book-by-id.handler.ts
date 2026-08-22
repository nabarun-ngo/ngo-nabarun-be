import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { GetBookByIdQuery } from './get-book-by-id.query';
import { IBookRepository } from '../../../domain/repositories/book.repository';
import { BookMapper } from '../../mappers/book.mapper';
import { BookDetailDto } from '../../dtos/book.dto';

@QueryHandler(GetBookByIdQuery)
@Injectable()
export class GetBookByIdHandler implements IQueryHandler<GetBookByIdQuery, BookDetailDto> {
  constructor(@Inject(IBookRepository) private readonly repo: IBookRepository) {}

  async execute(query: GetBookByIdQuery): Promise<BookDetailDto> {
    const book = await this.repo.findById(query.id);
    if (!book) {
      throw new BusinessException('Book not found with id ' + query.id);
    }
    return BookMapper.toDto(book);
  }
}
