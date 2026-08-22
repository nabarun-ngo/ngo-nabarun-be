import { IRepository } from '@nabarun-ngo/nestjs-shared-core';
import { Book, BookFilter } from '../aggregates/book/book.aggregate';

export const IBookRepository = Symbol('IBookRepository');

export interface IBookRepository extends IRepository<Book, string, BookFilter> {}
