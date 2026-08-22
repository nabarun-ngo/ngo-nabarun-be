import { Book } from '../../domain/aggregates/book/book.aggregate';
import { BookDetailDto } from '../dtos/book.dto';

export class BookMapper {
  static toDto(book: Book): BookDetailDto {
    return {
      id: book.id,
      title: book.title,
      author: book.author,
      category: book.category,
      subject: book.subject,
      classLevel: book.classLevel,
      isbn: book.isbn,
      location: book.location,
      status: book.status,
      acquisitionType: book.acquisitionType,
      acquisitionNotes: book.acquisitionNotes,
      holderUserId: book.holderUserId,
      holderGuestName: book.holderGuestName,
      createdById: book.createdById,
      updatedById: book.updatedById,
      loanHistory: book.loanHistory,
      createdAt: book.createdAt,
      updatedAt: book.updatedAt,
    };
  }
}
