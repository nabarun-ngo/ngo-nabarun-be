import { BookOperation } from '../../../domain/enums/book.enum';

export class ApplyBookOperationCommand {
  constructor(
    public readonly params: {
      id: string;
      operation: BookOperation;
      borrowerUserId?: string;
      guestName?: string;
      dueDate?: string;
      notes?: string;
      location?: string;
      actedById?: string;
    },
  ) {}
}
