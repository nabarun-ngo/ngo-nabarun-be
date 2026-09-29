import { Prisma } from '../../prisma/client';
import {
  Book,
  BookLoanRecordProps,
} from '../../../../modules/book-bank/domain/aggregates/book/book.aggregate';
import {
  BookAcquisitionType,
  BookCategory,
  BookClassLevel,
  BookStatus,
  BookSubject,
} from '../../../../modules/book-bank/domain/enums/book.enum';
import { MapperUtils } from '../../finance/mapper/mapper-utils';

export type BookPersistence = Prisma.BookGetPayload<{
  include: { loanHistory: true };
}>;

export type BookRow = Prisma.BookGetPayload<Record<string, never>>;
export type LoanRow = Prisma.BookLoanRecordGetPayload<Record<string, never>>;

export class BookPrismaMapper {
  static toDomain(row: BookRow | BookPersistence | null, loans?: LoanRow[]): Book | null {
    if (!row) return null;
    const history =
      ('loanHistory' in row && Array.isArray(row.loanHistory) ? row.loanHistory : loans) ?? [];

    return new Book(
      row.id,
      row.title,
      row.author,
      row.category as BookCategory,
      row.subject as BookSubject,
      row.classLevel as BookClassLevel,
      row.status as BookStatus,
      row.acquisitionType as BookAcquisitionType,
      MapperUtils.nullToUndefined(row.isbn),
      MapperUtils.nullToUndefined(row.location),
      MapperUtils.nullToUndefined(row.acquisitionNotes),
      MapperUtils.nullToUndefined(row.holderUserId),
      MapperUtils.nullToUndefined(row.holderGuestName),
      MapperUtils.nullToUndefined(row.createdById),
      MapperUtils.nullToUndefined(row.updatedById),
      history.map(BookPrismaMapper.loanToDomain),
      row.createdAt,
      row.updatedAt,
    );
  }

  static loanToDomain(row: LoanRow): BookLoanRecordProps {
    return {
      id: row.id,
      borrowerUserId: MapperUtils.nullToUndefined(row.borrowerUserId),
      guestName: MapperUtils.nullToUndefined(row.guestName),
      loanedAt: row.loanedAt,
      dueDate: MapperUtils.nullToUndefined(row.dueDate),
      returnedAt: MapperUtils.nullToUndefined(row.returnedAt),
      returnedById: MapperUtils.nullToUndefined(row.returnedById),
      notes: MapperUtils.nullToUndefined(row.notes),
    };
  }

  static toCreate(domain: Book): Prisma.BookUncheckedCreateInput {
    return {
      id: domain.id,
      title: domain.title,
      author: domain.author,
      category: domain.category,
      subject: domain.subject,
      classLevel: domain.classLevel,
      isbn: MapperUtils.undefinedToNull(domain.isbn),
      location: MapperUtils.undefinedToNull(domain.location),
      status: domain.status,
      acquisitionType: domain.acquisitionType,
      acquisitionNotes: MapperUtils.undefinedToNull(domain.acquisitionNotes),
      holderUserId: MapperUtils.undefinedToNull(domain.holderUserId),
      holderGuestName: MapperUtils.undefinedToNull(domain.holderGuestName),
      createdById: MapperUtils.undefinedToNull(domain.createdById),
      updatedById: MapperUtils.undefinedToNull(domain.updatedById),
      version: 0,
      loanHistory: {
        create: domain.loanHistory.map((r) => ({
          id: r.id,
          borrowerUserId: MapperUtils.undefinedToNull(r.borrowerUserId),
          guestName: MapperUtils.undefinedToNull(r.guestName),
          loanedAt: r.loanedAt,
          dueDate: MapperUtils.undefinedToNull(r.dueDate),
          returnedAt: MapperUtils.undefinedToNull(r.returnedAt),
          returnedById: MapperUtils.undefinedToNull(r.returnedById),
          notes: MapperUtils.undefinedToNull(r.notes),
        })),
      },
    };
  }

  static toUpdate(domain: Book): Prisma.BookUncheckedUpdateInput {
    return {
      title: domain.title,
      author: domain.author,
      category: domain.category,
      subject: domain.subject,
      classLevel: domain.classLevel,
      isbn: MapperUtils.undefinedToNull(domain.isbn),
      location: MapperUtils.undefinedToNull(domain.location),
      status: domain.status,
      acquisitionType: domain.acquisitionType,
      acquisitionNotes: MapperUtils.undefinedToNull(domain.acquisitionNotes),
      holderUserId: MapperUtils.undefinedToNull(domain.holderUserId),
      holderGuestName: MapperUtils.undefinedToNull(domain.holderGuestName),
      updatedById: MapperUtils.undefinedToNull(domain.updatedById),
      updatedAt: new Date(),
    };
  }
}
