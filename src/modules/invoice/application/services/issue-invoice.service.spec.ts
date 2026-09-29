import { IssueInvoiceService } from './issue-invoice.service';
import { Invoice } from '../../domain/aggregates/invoice/invoice.aggregate';
import { InvoiceEntityType } from '../../domain/enums/invoice-entity-type.enum';
import { InvoiceStatus } from '../../domain/enums/invoice-status.enum';
import { IInvoiceRepository } from '../../domain/repositories/invoice.repository';
import { IInvoiceDocumentStore } from '../../domain/ports/invoice-document-store.port';

describe('IssueInvoiceService', () => {
  const invoiceRepo: jest.Mocked<Pick<IInvoiceRepository, 'findIssuedByEntity' | 'findAll' | 'create' | 'update'>> = {
    findIssuedByEntity: jest.fn(),
    findAll: jest.fn(),
    create: jest.fn(async (invoice) => invoice),
    update: jest.fn(async (_id, invoice) => invoice),
  };
  const documentStore: jest.Mocked<Pick<IInvoiceDocumentStore, 'uploadDocument'>> = {
    uploadDocument: jest.fn().mockResolvedValue('doc-1'),
  };

  const service = new IssueInvoiceService(
    invoiceRepo as unknown as IInvoiceRepository,
    documentStore as unknown as IInvoiceDocumentStore,
  );

  beforeEach(() => {
    jest.clearAllMocks();
    invoiceRepo.findAll.mockResolvedValue([]);
    documentStore.uploadDocument.mockResolvedValue('doc-1');
  });

  it('uploads a caller-provided document mapped to the invoice', async () => {
    invoiceRepo.findIssuedByEntity.mockResolvedValue(null);
    const invoice = await service.issue({
      entityType: InvoiceEntityType.DONATION,
      entityId: 'NDON111111',
      amount: 500,
      currency: 'INR',
      issuedOn: new Date('2026-08-01'),
      documentFactory: async (issued) => ({
        buffer: Buffer.from('pdf'),
        fileName: `Receipt-${issued.entityId}.pdf`,
        relatedEntities: [
          { entityType: 'donation', entityId: 'NDON111111' },
          { entityType: 'donor', entityId: 'NDNR222222' },
        ],
      }),
    });
    expect(invoice.status).toBe(InvoiceStatus.ISSUED);
    expect(documentStore.uploadDocument).toHaveBeenCalledWith(
      expect.objectContaining({
        invoiceId: invoice.id,
        fileName: 'Receipt-NDON111111.pdf',
        relatedEntities: [
          { entityType: 'donation', entityId: 'NDON111111' },
          { entityType: 'donor', entityId: 'NDNR222222' },
        ],
      }),
    );
  });

  it('does not issue a second live invoice for the same entity', async () => {
    const existing = Invoice.issue({
      entityType: InvoiceEntityType.DONATION,
      entityId: 'NDON111111',
      amount: 500,
      currency: 'INR',
      issuedOn: new Date(),
    });
    invoiceRepo.findIssuedByEntity.mockResolvedValue(existing);
    await expect(
      service.issue({
        entityType: InvoiceEntityType.DONATION,
        entityId: 'NDON111111',
        amount: 500,
        currency: 'INR',
        issuedOn: new Date(),
        documentFactory: async () => ({ buffer: Buffer.from('pdf'), fileName: 'x.pdf' }),
      }),
    ).resolves.toBe(existing);
    expect(documentStore.uploadDocument).not.toHaveBeenCalled();
  });
});
