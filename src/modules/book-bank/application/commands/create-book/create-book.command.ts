import {
  BookAcquisitionType,
  BookCategory,
  BookClassLevel,
  BookSubject,
} from '../../../domain/enums/book.enum';

export class CreateBookCommand {
  constructor(
    public readonly params: {
      title: string;
      author: string;
      category: BookCategory;
      subject: BookSubject;
      classLevel: BookClassLevel;
      acquisitionType: BookAcquisitionType;
      isbn?: string;
      location?: string;
      acquisitionNotes?: string;
      createdById?: string;
    },
  ) {}
}
