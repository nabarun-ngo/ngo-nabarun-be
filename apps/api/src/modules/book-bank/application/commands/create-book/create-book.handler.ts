import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { CreateBookCommand } from './create-book.command';
import { Book } from '../../../domain/aggregates/book/book.aggregate';
import { IBookRepository } from '../../../domain/repositories/book.repository';

@CommandHandler(CreateBookCommand)
@Injectable()
export class CreateBookHandler implements ICommandHandler<CreateBookCommand, Book> {
  constructor(@Inject(IBookRepository) private readonly bookRepository: IBookRepository) {}

  async execute({ params }: CreateBookCommand): Promise<Book> {
    const book = Book.create(params);
    return this.bookRepository.create(book);
  }
}
