import { Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { GetBookReferenceDataQuery } from './get-book-reference-data.query';
import { BookReferenceDataDto } from '../../dtos/book.dto';
import {
  BookAcquisitionType,
  BookCategory,
  BookClassLevel,
  BookOperation,
  BookStatus,
  BookSubject,
} from '../../../domain/enums/book.enum';

@QueryHandler(GetBookReferenceDataQuery)
@Injectable()
export class GetBookReferenceDataHandler
  implements IQueryHandler<GetBookReferenceDataQuery, BookReferenceDataDto>
{
  async execute(): Promise<BookReferenceDataDto> {
    return {
      statuses: Object.values(BookStatus),
      categories: Object.values(BookCategory),
      subjects: Object.values(BookSubject),
      classLevels: Object.values(BookClassLevel),
      acquisitionTypes: Object.values(BookAcquisitionType),
      operations: Object.values(BookOperation),
    };
  }
}
