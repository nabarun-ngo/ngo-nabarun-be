import { Invoice } from './invoice.aggregate';
import { InvoiceEntityType } from '../../enums/invoice-entity-type.enum';
import { InvoiceStatus } from '../../enums/invoice-status.enum';

describe('Invoice', () => {
  it('issues a unique NREC receipt for a donation', () => {
    const invoice = Invoice.issue({
      entityType: InvoiceEntityType.DONATION,
      entityId: 'NDON123456',
      amount: 500,
      currency: 'INR',
      issuedOn: new Date('2026-08-23'),
    });
    expect(invoice.id).toMatch(/^NREC\d{6}$/);
    expect(invoice.status).toBe(InvoiceStatus.ISSUED);
    expect(invoice.entityType).toBe(InvoiceEntityType.DONATION);
    expect(invoice.entityId).toBe('NDON123456');
  });

  it('voids an issued receipt and clears the file link', () => {
    const invoice = Invoice.issue({
      entityType: InvoiceEntityType.DONATION,
      entityId: 'NDON123456',
      amount: 500,
      currency: 'INR',
      issuedOn: new Date(),
    });
    invoice.attachDocument('doc-1');
    invoice.void('Update mistake');
    expect(invoice.status).toBe(InvoiceStatus.VOIDED);
    expect(invoice.documentId).toBeUndefined();
    expect(invoice.voidReason).toBe('Update mistake');
  });
});
