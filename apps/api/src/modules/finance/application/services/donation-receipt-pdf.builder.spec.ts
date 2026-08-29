import { DocumentGeneratorService } from '@nabarun-ngo/nestjs-shared-document-generator';
import { DonationReceiptPdfBuilder } from './donation-receipt-pdf.builder';

describe('DonationReceiptPdfBuilder', () => {
  it('prints invoice number, donation number, and transaction reference number', async () => {
    const pdf = {
      setOptions: jest.fn().mockReturnThis(),
      addSection: jest.fn().mockReturnThis(),
      addHeading: jest.fn().mockReturnThis(),
      addTable: jest.fn().mockReturnThis(),
      addParagraph: jest.fn().mockReturnThis(),
      endSection: jest.fn().mockReturnThis(),
      build: jest.fn().mockResolvedValue(Buffer.from('pdf')),
    };
    const documentGenerator = {
      createPdfBuilder: jest.fn().mockReturnValue(pdf),
    };
    const donation = {
      id: 'NDON111111',
      transactionRef: 'TXR1234567890',
      amount: 500,
      currency: 'INR',
      paidOn: new Date('2026-08-01'),
    } as any;

    await new DonationReceiptPdfBuilder(
      documentGenerator as unknown as DocumentGeneratorService,
    ).build({
      invoiceId: 'NREC000001',
      issuedOn: new Date('2026-08-01'),
      donation,
      donorName: 'Asha Verma',
    });

    expect(pdf.addTable).toHaveBeenCalledWith([
      ['Invoice number', 'NREC000001'],
      ['Donation number', 'NDON111111'],
      ['Transaction reference number', 'TXR1234567890'],
    ]);
  });
});
