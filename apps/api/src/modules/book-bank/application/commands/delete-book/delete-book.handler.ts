import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { DeleteBookCommand } from './delete-book.command';
import { IBookRepository } from '../../../domain/repositories/book.repository';

@CommandHandler(DeleteBookCommand)
@Injectable()
export class DeleteBookHandler implements ICommandHandler<DeleteBookCommand, void> {
  constructor(@Inject(IBookRepository) private readonly bookRepository: IBookRepository) {}

  async execute({ id }: DeleteBookCommand): Promise<void> {
    const book = await this.bookRepository.findById(id);
    if (!book) {
      throw new BusinessException('Book not found with id ' + id);
    }
    await this.bookRepository.delete(book.id);
  }
}
