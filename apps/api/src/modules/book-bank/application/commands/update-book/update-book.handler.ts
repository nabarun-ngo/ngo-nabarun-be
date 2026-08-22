import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { UpdateBookCommand } from './update-book.command';
import { Book } from '../../../domain/aggregates/book/book.aggregate';
import { IBookRepository } from '../../../domain/repositories/book.repository';

@CommandHandler(UpdateBookCommand)
@Injectable()
export class UpdateBookHandler implements ICommandHandler<UpdateBookCommand, Book> {
  constructor(@Inject(IBookRepository) private readonly bookRepository: IBookRepository) {}

  async execute({ params }: UpdateBookCommand): Promise<Book> {
    const book = await this.bookRepository.findById(params.id);
    if (!book) {
      throw new BusinessException('Book not found with id ' + params.id);
    }

    book.update({
      title: params.title,
      author: params.author,
      category: params.category,
      subject: params.subject,
      classLevel: params.classLevel,
      acquisitionType: params.acquisitionType,
      isbn: params.isbn,
      location: params.location,
      acquisitionNotes: params.acquisitionNotes,
      updatedById: params.updatedById,
    });

    return this.bookRepository.update(book.id, book);
  }
}
