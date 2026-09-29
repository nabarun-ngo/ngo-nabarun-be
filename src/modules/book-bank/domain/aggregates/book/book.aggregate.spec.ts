import { Book } from './book.aggregate';
import {
  BookAcquisitionType,
  BookCategory,
  BookClassLevel,
  BookStatus,
  BookSubject,
} from '../../enums/book.enum';

function makeBook() {
  return Book.create({
    title: 'NCERT Mathematics',
    author: 'NCERT',
    category: BookCategory.TEXTBOOK,
    subject: BookSubject.MATH,
    classLevel: BookClassLevel.CLASS_8,
    acquisitionType: BookAcquisitionType.PURCHASED,
    location: 'School office cupboard',
  });
}

describe('Book aggregate', () => {
  it('creates as AVAILABLE', () => {
    const book = makeBook();
    expect(book.status).toBe(BookStatus.AVAILABLE);
  });

  it('lends to a member and returns', () => {
    const book = makeBook();
    book.lend({ borrowerUserId: 'user-1', actedById: 'admin-1' });
    expect(book.status).toBe(BookStatus.ON_LOAN);
    expect(book.holderUserId).toBe('user-1');
    expect(book.loanHistory).toHaveLength(1);

    book.returnLoan('admin-1');
    expect(book.status).toBe(BookStatus.AVAILABLE);
    expect(book.holderUserId).toBeUndefined();
    expect(book.loanHistory[0].returnedAt).toBeDefined();
  });

  it('lends to a guest', () => {
    const book = makeBook();
    book.lend({ guestName: 'Ananya', actedById: 'admin-1' });
    expect(book.holderGuestName).toBe('Ananya');
    expect(book.status).toBe(BookStatus.ON_LOAN);
  });

  it('rejects lend without recipient', () => {
    const book = makeBook();
    expect(() => book.lend({})).toThrow(/Recipient/);
  });

  it('donates out permanently', () => {
    const book = makeBook();
    book.donateOut({ guestName: 'Local library', actedById: 'admin-1' });
    expect(book.status).toBe(BookStatus.DONATED_OUT);
    expect(() => book.lend({ borrowerUserId: 'user-1' })).toThrow(/available/i);
  });

  it('transfers descriptive location', () => {
    const book = makeBook();
    book.transferLocation('Ramesh house — shelf B');
    expect(book.location).toBe('Ramesh house — shelf B');
  });

  it('rejects return when not on loan', () => {
    const book = makeBook();
    expect(() => book.returnLoan()).toThrow(/on loan/i);
  });
});
