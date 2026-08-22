import { randomUUID } from 'crypto';
import { AggregateRoot, BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import {
  BookAcquisitionType,
  BookCategory,
  BookClassLevel,
  BookStatus,
  BookSubject,
} from '../../enums/book.enum';

export interface BookLoanRecordProps {
  id: string;
  borrowerUserId?: string;
  guestName?: string;
  loanedAt: Date;
  dueDate?: Date;
  returnedAt?: Date;
  returnedById?: string;
  notes?: string;
}

export interface BookFilter {
  status?: string;
  category?: string;
  author?: string;
  subject?: string;
  classLevel?: string;
  location?: string;
  holderUserId?: string;
  acquisitionType?: string;
  q?: string;
}

export interface BookCreateProps {
  title: string;
  author: string;
  category: BookCategory;
  subject: BookSubject;
  classLevel: BookClassLevel;
  isbn?: string;
  location?: string;
  acquisitionType: BookAcquisitionType;
  acquisitionNotes?: string;
  createdById?: string;
}

export interface BookUpdateProps {
  title?: string;
  author?: string;
  category?: BookCategory;
  subject?: BookSubject;
  classLevel?: BookClassLevel;
  isbn?: string | null;
  location?: string | null;
  acquisitionType?: BookAcquisitionType;
  acquisitionNotes?: string | null;
  updatedById?: string;
}

export interface BookRecipientProps {
  borrowerUserId?: string;
  guestName?: string;
  dueDate?: Date;
  notes?: string;
  actedById?: string;
}

export class Book extends AggregateRoot<string> {
  #title: string;
  #author: string;
  #category: BookCategory;
  #subject: BookSubject;
  #classLevel: BookClassLevel;
  #isbn?: string;
  #location?: string;
  #status: BookStatus;
  #acquisitionType: BookAcquisitionType;
  #acquisitionNotes?: string;
  #holderUserId?: string;
  #holderGuestName?: string;
  #createdById?: string;
  #updatedById?: string;
  #loanHistory: BookLoanRecordProps[];

  constructor(
    id: string,
    title: string,
    author: string,
    category: BookCategory,
    subject: BookSubject,
    classLevel: BookClassLevel,
    status: BookStatus,
    acquisitionType: BookAcquisitionType,
    isbn: string | undefined,
    location: string | undefined,
    acquisitionNotes: string | undefined,
    holderUserId: string | undefined,
    holderGuestName: string | undefined,
    createdById: string | undefined,
    updatedById: string | undefined,
    loanHistory: BookLoanRecordProps[] | undefined,
    createdAt?: Date,
    updatedAt?: Date,
  ) {
    super(id, createdAt, updatedAt);
    this.#title = title;
    this.#author = author;
    this.#category = category;
    this.#subject = subject;
    this.#classLevel = classLevel;
    this.#status = status;
    this.#acquisitionType = acquisitionType;
    this.#isbn = isbn;
    this.#location = location;
    this.#acquisitionNotes = acquisitionNotes;
    this.#holderUserId = holderUserId;
    this.#holderGuestName = holderGuestName;
    this.#createdById = createdById;
    this.#updatedById = updatedById;
    this.#loanHistory = loanHistory ?? [];
  }

  static create(props: BookCreateProps): Book {
    if (!props.title?.trim()) {
      throw new BusinessException('Book title is required');
    }
    if (!props.author?.trim()) {
      throw new BusinessException('Book author is required');
    }

    return new Book(
      randomUUID(),
      props.title.trim(),
      props.author.trim(),
      props.category,
      props.subject,
      props.classLevel,
      BookStatus.AVAILABLE,
      props.acquisitionType,
      props.isbn?.trim() || undefined,
      props.location?.trim() || undefined,
      props.acquisitionNotes?.trim() || undefined,
      undefined,
      undefined,
      props.createdById,
      undefined,
      [],
    );
  }

  update(props: BookUpdateProps): void {
    if (props.title !== undefined) {
      if (!props.title.trim()) {
        throw new BusinessException('Book title is required');
      }
      this.#title = props.title.trim();
    }
    if (props.author !== undefined) {
      if (!props.author.trim()) {
        throw new BusinessException('Book author is required');
      }
      this.#author = props.author.trim();
    }
    if (props.category !== undefined) this.#category = props.category;
    if (props.subject !== undefined) this.#subject = props.subject;
    if (props.classLevel !== undefined) this.#classLevel = props.classLevel;
    if (props.isbn !== undefined) this.#isbn = props.isbn?.trim() || undefined;
    if (props.location !== undefined) this.#location = props.location?.trim() || undefined;
    if (props.acquisitionType !== undefined) this.#acquisitionType = props.acquisitionType;
    if (props.acquisitionNotes !== undefined) {
      this.#acquisitionNotes = props.acquisitionNotes?.trim() || undefined;
    }
    if (props.updatedById !== undefined) this.#updatedById = props.updatedById;
    this.touch();
  }

  lend(props: BookRecipientProps): void {
    if (this.#status !== BookStatus.AVAILABLE) {
      throw new BusinessException('Only available books can be lent');
    }
    const recipient = Book.assertRecipient(props.borrowerUserId, props.guestName);

    this.#loanHistory.push({
      id: randomUUID(),
      borrowerUserId: recipient.borrowerUserId,
      guestName: recipient.guestName,
      loanedAt: new Date(),
      dueDate: props.dueDate,
      notes: props.notes,
    });
    this.#holderUserId = recipient.borrowerUserId;
    this.#holderGuestName = recipient.guestName;
    this.#status = BookStatus.ON_LOAN;
    this.#updatedById = props.actedById;
    this.touch();
  }

  returnLoan(actedById?: string, notes?: string): void {
    if (this.#status !== BookStatus.ON_LOAN) {
      throw new BusinessException('Only books on loan can be returned');
    }
    const open = this.#loanHistory.find((r) => !r.returnedAt);
    if (!open) {
      throw new BusinessException('Book has no open loan to return');
    }

    open.returnedAt = new Date();
    open.returnedById = actedById;
    if (notes !== undefined) {
      open.notes = open.notes ? `${open.notes}\n${notes}` : notes;
    }

    this.#holderUserId = undefined;
    this.#holderGuestName = undefined;
    this.#status = BookStatus.AVAILABLE;
    this.#updatedById = actedById;
    this.touch();
  }

  donateOut(props: BookRecipientProps): void {
    if (this.#status !== BookStatus.AVAILABLE) {
      throw new BusinessException('Only available books can be donated out');
    }
    const recipient = Book.assertRecipient(props.borrowerUserId, props.guestName);

    this.#holderUserId = recipient.borrowerUserId;
    this.#holderGuestName = recipient.guestName;
    this.#status = BookStatus.DONATED_OUT;
    this.#updatedById = props.actedById;
    if (props.notes?.trim()) {
      const note = props.notes.trim();
      this.#acquisitionNotes = this.#acquisitionNotes
        ? `${this.#acquisitionNotes}\nDonated out: ${note}`
        : `Donated out: ${note}`;
    }
    this.touch();
  }

  retire(actedById?: string, notes?: string): void {
    if (this.#status === BookStatus.ON_LOAN) {
      throw new BusinessException('Return the book before retiring it');
    }
    if (this.#status === BookStatus.DONATED_OUT) {
      throw new BusinessException('Donated-out books cannot be retired');
    }
    this.#status = BookStatus.RETIRED;
    this.#updatedById = actedById;
    if (notes?.trim()) {
      this.#acquisitionNotes = this.#acquisitionNotes
        ? `${this.#acquisitionNotes}\nRetired: ${notes.trim()}`
        : `Retired: ${notes.trim()}`;
    }
    this.touch();
  }

  markLost(actedById?: string, notes?: string): void {
    if (this.#status === BookStatus.DONATED_OUT) {
      throw new BusinessException('Donated-out books cannot be marked lost');
    }
    if (this.#status === BookStatus.ON_LOAN) {
      const open = this.#loanHistory.find((r) => !r.returnedAt);
      if (open) {
        open.returnedAt = new Date();
        open.returnedById = actedById;
        if (notes !== undefined) {
          open.notes = open.notes ? `${open.notes}\n${notes}` : notes;
        }
      }
      this.#holderUserId = undefined;
      this.#holderGuestName = undefined;
    }
    this.#status = BookStatus.LOST;
    this.#updatedById = actedById;
    this.touch();
  }

  transferLocation(location: string, actedById?: string): void {
    if (!location?.trim()) {
      throw new BusinessException('Location description is required to move a book');
    }
    if (this.#status === BookStatus.DONATED_OUT) {
      throw new BusinessException('Donated-out books cannot change location in the bank');
    }
    this.#location = location.trim();
    this.#updatedById = actedById;
    this.touch();
  }

  private static assertRecipient(
    borrowerUserId?: string,
    guestName?: string,
  ): { borrowerUserId?: string; guestName?: string } {
    const member = borrowerUserId?.trim() || undefined;
    const guest = guestName?.trim() || undefined;
    if (!member && !guest) {
      throw new BusinessException('Recipient member or guest name is required');
    }
    if (member && guest) {
      throw new BusinessException('Provide either a member or a guest name, not both');
    }
    return { borrowerUserId: member, guestName: guest };
  }

  get title(): string { return this.#title; }
  get author(): string { return this.#author; }
  get category(): BookCategory { return this.#category; }
  get subject(): BookSubject { return this.#subject; }
  get classLevel(): BookClassLevel { return this.#classLevel; }
  get isbn(): string | undefined { return this.#isbn; }
  get location(): string | undefined { return this.#location; }
  get status(): BookStatus { return this.#status; }
  get acquisitionType(): BookAcquisitionType { return this.#acquisitionType; }
  get acquisitionNotes(): string | undefined { return this.#acquisitionNotes; }
  get holderUserId(): string | undefined { return this.#holderUserId; }
  get holderGuestName(): string | undefined { return this.#holderGuestName; }
  get createdById(): string | undefined { return this.#createdById; }
  get updatedById(): string | undefined { return this.#updatedById; }
  get loanHistory(): BookLoanRecordProps[] {
    return this.#loanHistory.map((r) => ({ ...r }));
  }
}
