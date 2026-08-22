import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { ApplyBookOperationCommand } from './apply-book-operation.command';
import { Book } from '../../../domain/aggregates/book/book.aggregate';
import { IBookRepository } from '../../../domain/repositories/book.repository';
import { BookOperation } from '../../../domain/enums/book.enum';

@CommandHandler(ApplyBookOperationCommand)
@Injectable()
export class ApplyBookOperationHandler
  implements ICommandHandler<ApplyBookOperationCommand, Book>
{
  constructor(@Inject(IBookRepository) private readonly bookRepository: IBookRepository) {}

  async execute({ params }: ApplyBookOperationCommand): Promise<Book> {
    const book = await this.bookRepository.findById(params.id);
    if (!book) {
      throw new BusinessException('Book not found with id ' + params.id);
    }

    const actedById = params.actedById;
    const dueDate = params.dueDate ? new Date(params.dueDate) : undefined;

    switch (params.operation) {
      case BookOperation.LEND:
        book.lend({
          borrowerUserId: params.borrowerUserId,
          guestName: params.guestName,
          dueDate,
          notes: params.notes,
          actedById,
        });
        break;
      case BookOperation.RETURN:
        book.returnLoan(actedById, params.notes);
        break;
      case BookOperation.DONATE_OUT:
        book.donateOut({
          borrowerUserId: params.borrowerUserId,
          guestName: params.guestName,
          notes: params.notes,
          actedById,
        });
        break;
      case BookOperation.RETIRE:
        book.retire(actedById, params.notes);
        break;
      case BookOperation.MARK_LOST:
        book.markLost(actedById, params.notes);
        break;
      case BookOperation.TRANSFER_LOCATION:
        book.transferLocation(params.location ?? '', actedById);
        break;
      default:
        throw new BusinessException(`Unsupported book operation: ${params.operation}`);
    }

    return this.bookRepository.update(book.id, book);
  }
}
