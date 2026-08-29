import { DonationInvoiceService } from './donation-invoice.service';
import { InvoiceFacade } from '../../../invoice/application/services/invoice.facade';
import { IDonorRepository } from '../../domain/repositories/donor.repository';
import { IUserLookupPort } from '@nabarun-ngo/nestjs-shared-core';
import { DocumentGeneratorService } from '@nabarun-ngo/nestjs-shared-document-generator';
import { InvoiceEntityType } from '../../../invoice/domain/enums/invoice-entity-type.enum';
import { EntityType } from '../../../../shared/enums/entity-type.enum';

describe('DonationInvoiceService', () => {
  const invoiceFacade: jest.Mocked<Pick<InvoiceFacade, 'issue' | 'voidIssued'>> = {
    issue: jest.fn(),
    voidIssued: jest.fn(),
  };
  const donorRepo: jest.Mocked<Pick<IDonorRepository, 'findById'>> = {
    findById: jest.fn().mockResolvedValue({ fullName: 'Asha Verma' }),
  };
  const userLookup: jest.Mocked<Pick<IUserLookupPort, 'findById'>> = {
    findById: jest.fn(),
  };
  const pdfBuilder = {
    addSection: jest.fn().mockReturnThis(),
    addHeading: jest.fn().mockReturnThis(),
    addParagraph: jest.fn().mockReturnThis(),
    addTable: jest.fn().mockReturnThis(),
    endSection: jest.fn().mockReturnThis(),
    setOptions: jest.fn().mockReturnThis(),
    build: jest.fn().mockResolvedValue(Buffer.from('pdf')),
  };
  const documentGenerator = {
    createPdfBuilder: jest.fn().mockReturnValue(pdfBuilder),
  };

  const service = new DonationInvoiceService(
    invoiceFacade as unknown as InvoiceFacade,
    donorRepo as unknown as IDonorRepository,
    userLookup as unknown as IUserLookupPort,
    documentGenerator as unknown as DocumentGeneratorService,
  );

  const donation = {
    id: 'NDON111111',
    donorId: 'NDNR222222',
    amount: 500,
    currency: 'INR',
    paidOn: new Date('2026-08-01'),
    transactionRef: 'TXR1234567890',
  } as any;

  let lastDocumentPayload: unknown;

  beforeEach(() => {
    jest.clearAllMocks();
    lastDocumentPayload = undefined;
    invoiceFacade.issue.mockImplementation(async (params) => {
      const summary = {
        id: 'NREC000001',
        entityId: params.entityId,
        status: 'ISSUED' as const,
        issuedOn: params.issuedOn,
      };
      if (params.documentFactory) {
        lastDocumentPayload = await params.documentFactory(summary as any);
      }
      return summary as any;
    });
    documentGenerator.createPdfBuilder.mockReturnValue(pdfBuilder);
  });

  it('asks invoice to issue a donation receipt with donation and donor mappings', async () => {
    await service.issueForPaidDonation(donation);
    expect(invoiceFacade.issue).toHaveBeenCalledWith(
      expect.objectContaining({
        entityType: InvoiceEntityType.DONATION,
        entityId: 'NDON111111',
        amount: 500,
      }),
    );
    expect(pdfBuilder.addTable).toHaveBeenCalledWith([
      ['Invoice number', 'NREC000001'],
      ['Donation number', 'NDON111111'],
      ['Transaction reference number', 'TXR1234567890'],
    ]);
    expect(lastDocumentPayload).toEqual(
      expect.objectContaining({
        fileName: 'Receipt-NDON111111.pdf',
        relatedEntities: [
          { entityType: EntityType.Donation, entityId: 'NDON111111' },
          { entityType: EntityType.Donor, entityId: 'NDNR222222' },
        ],
      }),
    );
  });

  it('does not issue a receipt without a transaction reference', async () => {
    await expect(
      service.issueForPaidDonation({ ...donation, transactionRef: undefined }),
    ).rejects.toThrow('Cannot issue a receipt without a transaction reference');
    expect(invoiceFacade.issue).not.toHaveBeenCalled();
  });
});
