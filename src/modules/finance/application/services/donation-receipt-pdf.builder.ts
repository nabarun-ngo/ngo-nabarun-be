import { formatDate } from '@ssdev-toolkit/nestjs-core';
import { DocumentGeneratorService } from '@ssdev-toolkit/nestjs-document-generator';
import { Donation } from '../../domain/aggregates/donation/donation.aggregate';

export class DonationReceiptPdfBuilder {
  constructor(private readonly documentGenerator: DocumentGeneratorService) {}

  build(params: {
    invoiceId: string;
    issuedOn: Date;
    donation: Donation;
    donorName: string;
  }): Promise<Buffer> {
    const { invoiceId, issuedOn, donation, donorName } = params;
    const paidOn = donation.paidOn ? formatDate(donation.paidOn) : formatDate(issuedOn);
    const pdf = this.documentGenerator.createPdfBuilder('pdfkit');
    pdf.setOptions({ pageSize: 'A4', orientation: 'portrait' });
    pdf
      .addSection('Donation receipt')
      .addHeading('Donation receipt', 1)
      .addTable([
        ['Invoice number', invoiceId],
        ['Donation number', donation.id],
        ['Transaction reference number', donation.transactionRef ?? '—'],
      ])
      .addParagraph(`Donor ${donorName}`)
      .addParagraph(`Amount ${donation.currency} ${donation.amount}`)
      .addParagraph(`Paid on ${paidOn}`)
      .endSection();
    return pdf.build();
  }
}
