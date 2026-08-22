import {
  BookAcquisitionType,
  BookCategory,
  BookClassLevel,
  BookSubject,
} from '../../../domain/enums/book.enum';

export class UpdateBookCommand {
  constructor(
    public readonly params: {
      id: string;
      title?: string;
      author?: string;
      category?: BookCategory;
      subject?: BookSubject;
      classLevel?: BookClassLevel;
      acquisitionType?: BookAcquisitionType;
      isbn?: string;
      location?: string;
      acquisitionNotes?: string;
      updatedById?: string;
    },
  ) {}
}
