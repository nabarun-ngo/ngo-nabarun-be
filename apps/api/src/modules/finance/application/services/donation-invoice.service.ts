import { Inject, Injectable } from '@nestjs/common';
import { BusinessException, IUserLookupPort } from '@nabarun-ngo/nestjs-shared-core';
import { DocumentGeneratorService } from '@nabarun-ngo/nestjs-shared-document-generator';
import { InvoiceEntityType } from '../../../invoice/domain/enums/invoice-entity-type.enum';
import { InvoiceFacade } from '../../../invoice/application/services/invoice.facade';
import { Donation } from '../../domain/aggregates/donation/donation.aggregate';
import { IDonorRepository } from '../../domain/repositories/donor.repository';
import { EntityType } from '../../../../shared/enums/entity-type.enum';
import { DonationReceiptPdfBuilder } from './donation-receipt-pdf.builder';

@Injectable()
export class DonationInvoiceService {
  constructor(
    private readonly invoiceFacade: InvoiceFacade,
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    @Inject(IUserLookupPort) private readonly userLookup: IUserLookupPort,
    private readonly documentGenerator: DocumentGeneratorService,
  ) {}

  async issueForPaidDonation(donation: Donation): Promise<void> {
    if (!donation.donorId) {
      throw new BusinessException('Cannot issue a receipt without a donor');
    }
    if (!donation.transactionRef) {
      throw new BusinessException('Cannot issue a receipt without a transaction reference');
    }

    await this.invoiceFacade.issue({
      entityType: InvoiceEntityType.DONATION,
      entityId: donation.id,
      amount: donation.amount,
      currency: donation.currency,
      issuedOn: donation.paidOn ?? new Date(),
      documentFactory: async (invoice) => {
        const donorName = await this.resolveDonorName(donation.donorId!);
        const buffer = await new DonationReceiptPdfBuilder(this.documentGenerator).build({
          invoiceId: invoice.id,
          issuedOn: invoice.issuedOn,
          donation,
          donorName,
        });
        return {
          buffer,
          fileName: `Receipt-${donation.id}.pdf`,
          relatedEntities: [
            { entityType: EntityType.Donation, entityId: donation.id },
            { entityType: EntityType.Donor, entityId: donation.donorId! },
          ],
        };
      },
    });
  }

  voidForDonation(donationId: string, reason: string): Promise<void> {
    return this.invoiceFacade.voidIssued(InvoiceEntityType.DONATION, donationId, reason);
  }

  private async resolveDonorName(donorId: string): Promise<string> {
    const donor = await this.donorRepository.findById(donorId);
    if (donor?.fullName) return donor.fullName;
    if (donor?.userProfileId) {
      const user = await this.userLookup.findById(donor.userProfileId);
      const name = user?.fullName?.trim();
      if (name) return name;
    }
    return donorId;
  }
}
