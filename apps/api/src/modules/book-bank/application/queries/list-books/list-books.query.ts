import { BookDetailFilterDto } from '../../dtos/book.dto';

export class ListBooksQuery {
  constructor(
    public readonly filter: BookDetailFilterDto = {},
    public readonly pageIndex?: number,
    public readonly pageSize?: number,
  ) {}
}
