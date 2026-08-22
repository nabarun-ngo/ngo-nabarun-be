import { Injectable } from '@nestjs/common';
import { BaseFilter, Page } from '@nabarun-ngo/nestjs-shared-core';
import { BasePrismaService } from '@nabarun-ngo/nestjs-shared-persistence';
import { Prisma, PrismaClient } from '../../prisma/client';
import { Book, BookFilter } from '../../../../modules/book-bank/domain/aggregates/book/book.aggregate';
import { IBookRepository } from '../../../../modules/book-bank/domain/repositories/book.repository';
import { BookPrismaMapper } from '../mapper/book-prisma.mapper';
import { MapperUtils } from '../../finance/mapper/mapper-utils';

@Injectable()
export class BookPrismaRepository implements IBookRepository {
  constructor(private readonly database: BasePrismaService<PrismaClient>) {}

  async count(filter: BookFilter): Promise<number> {
    return this.database.client.book.count({ where: this.where(filter) });
  }

  async findPaged(filter?: BaseFilter<BookFilter>): Promise<Page<Book>> {
    const where = this.where(filter?.props);
    const pageIndex = filter?.pageIndex ?? 0;
    const pageSize = filter?.pageSize ?? 20;
    const [rows, total] = await Promise.all([
      this.database.client.book.findMany({
        where,
        orderBy: { createdAt: 'desc' },
        skip: pageIndex * pageSize,
        take: pageSize,
      }),
      this.database.client.book.count({ where }),
    ]);
    return new Page(
      rows.map((r) => BookPrismaMapper.toDomain(r)!),
      total,
      pageIndex,
      pageSize,
    );
  }

  async findAll(filter?: BookFilter): Promise<Book[]> {
    const rows = await this.database.client.book.findMany({
      where: this.where(filter),
      orderBy: { createdAt: 'desc' },
    });
    return rows.map((r) => BookPrismaMapper.toDomain(r)!);
  }

  async findById(id: string): Promise<Book | null> {
    const row = await this.database.client.book.findFirst({
      where: { id, deletedAt: null },
      include: { loanHistory: { orderBy: { loanedAt: 'desc' } } },
    });
    return BookPrismaMapper.toDomain(row);
  }

  async create(entity: Book): Promise<Book> {
    const row = await this.database.client.book.create({
      data: BookPrismaMapper.toCreate(entity),
      include: { loanHistory: { orderBy: { loanedAt: 'desc' } } },
    });
    return BookPrismaMapper.toDomain(row)!;
  }

  async update(id: string, entity: Book): Promise<Book> {
    await this.database.client.$transaction(async (tx) => {
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

  async delete(id: string): Promise<void> {
    await this.database.client.book.update({
      where: { id },
      data: { deletedAt: new Date() },
    });
  }

  private where(props?: BookFilter): Prisma.BookWhereInput {
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
}
