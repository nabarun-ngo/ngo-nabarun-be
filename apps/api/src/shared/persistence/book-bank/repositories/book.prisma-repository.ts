import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { PrismaClient } from '../../prisma/client';
import type {
  BookWhereInput,
  BookWhereUniqueInput,
  BookUncheckedCreateInput,
  BookUncheckedUpdateInput,
  BookOrderByWithRelationInput,
} from '../../prisma/models/Book';
import { Book, BookFilter } from '../../../../modules/book-bank/domain/aggregates/book/book.aggregate';
import { IBookRepository } from '../../../../modules/book-bank/domain/repositories/book.repository';
import { BookPrismaMapper, BookRow } from '../mapper/book-prisma.mapper';
import { MapperUtils } from '../../finance/mapper/mapper-utils';

@Injectable()
export class BookPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'book',
    Book,
    string,
    BookFilter,
    BookRow,
    BookWhereInput,
    BookWhereUniqueInput,
    BookUncheckedCreateInput,
    BookUncheckedUpdateInput,
    BookOrderByWithRelationInput
  >
  implements IBookRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'book');
  }

  async findById(id: string): Promise<Book | null> {
    const row = await this.delegate.findFirst({
      where: { id, deletedAt: null },
      include: { loanHistory: { orderBy: { loanedAt: 'desc' } } },
    });
    return BookPrismaMapper.toDomain(row);
  }

  async create(entity: Book): Promise<Book> {
    const row = await this.delegate.create({
      data: BookPrismaMapper.toCreate(entity),
      include: { loanHistory: { orderBy: { loanedAt: 'desc' } } },
    });
    return BookPrismaMapper.toDomain(row)!;
  }

  async update(id: string, entity: Book): Promise<Book> {
    await this.$transaction(async (tx) => {
      await tx.book.update({
        where: { id },
        data: BookPrismaMapper.toUpdate(entity),
      });

      const existing = await tx.bookLoanRecord.findMany({ where: { bookId: id } });
      const existingIds = new Set(existing.map((r) => r.id));
      const desired = entity.loanHistory;

      for (const record of desired) {
        const data = {
          borrowerUserId: MapperUtils.undefinedToNull(record.borrowerUserId),
          guestName: MapperUtils.undefinedToNull(record.guestName),
          loanedAt: record.loanedAt,
          dueDate: MapperUtils.undefinedToNull(record.dueDate),
          returnedAt: MapperUtils.undefinedToNull(record.returnedAt),
          returnedById: MapperUtils.undefinedToNull(record.returnedById),
          notes: MapperUtils.undefinedToNull(record.notes),
        };
        if (existingIds.has(record.id)) {
          await tx.bookLoanRecord.update({ where: { id: record.id }, data });
          existingIds.delete(record.id);
        } else {
          await tx.bookLoanRecord.create({
            data: { id: record.id, bookId: id, ...data },
          });
        }
      }
    });

    return (await this.findById(id))!;
  }

  protected toDomain(row: BookRow): Book {
    return BookPrismaMapper.toDomain(row)!;
  }

  protected toCreateInput(entity: Book): BookUncheckedCreateInput {
    return BookPrismaMapper.toCreate(entity);
  }

  protected toUpdateInput(_id: string, entity: Book): BookUncheckedUpdateInput {
    return BookPrismaMapper.toUpdate(entity);
  }

  protected toUniqueWhere(id: string): BookWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(props?: BookFilter): BookWhereInput {
    const q = props?.q?.trim();
    return {
      deletedAt: null,
      ...(props?.status ? { status: props.status } : {}),
      ...(props?.category ? { category: props.category } : {}),
      ...(props?.subject ? { subject: props.subject } : {}),
      ...(props?.classLevel ? { classLevel: props.classLevel } : {}),
      ...(props?.holderUserId ? { holderUserId: props.holderUserId } : {}),
      ...(props?.acquisitionType ? { acquisitionType: props.acquisitionType } : {}),
      ...(props?.author?.trim()
        ? { author: { contains: props.author.trim(), mode: 'insensitive' } }
        : {}),
      ...(props?.location?.trim()
        ? { location: { contains: props.location.trim(), mode: 'insensitive' } }
        : {}),
      ...(q
        ? {
            OR: [
              { title: { contains: q, mode: 'insensitive' } },
              { author: { contains: q, mode: 'insensitive' } },
              { isbn: { contains: q, mode: 'insensitive' } },
              { location: { contains: q, mode: 'insensitive' } },
            ],
          }
        : {}),
    };
  }

  protected override supportsSoftDelete(): boolean {
    return true;
  }

  protected override defaultOrderBy(): BookOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected defaultPageSize(): number {
    return 20;
  }
}
