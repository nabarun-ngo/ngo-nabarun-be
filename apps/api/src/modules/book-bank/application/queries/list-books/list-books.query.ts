import { BookDetailFilterDto } from '../../dtos/book.dto';

export class ListBooksQuery {
  constructor(
    public readonly filter: BookDetailFilterDto = {},
  ) {}
}
