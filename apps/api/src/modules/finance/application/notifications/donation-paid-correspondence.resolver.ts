import { Inject, Injectable, Logger } from '@nestjs/common';
import { formatDate } from '@nabarun-ngo/nestjs-shared-core';
import {
  ICorrespondenceEventResolver,
  CorrespondenceEventResolver,
  NotificationSpec,
  NotificationType,
  NotificationPriority,
} from '@nabarun-ngo/nestjs-shared-correspondence';
import { DonationPaidEvent } from '../../domain/events/donation-paid.event';
import { financeUserFullName, FinanceUserRef } from '../../domain/types/finance-user-ref';
import { IDonationRepository } from '../../domain/repositories/donation.repository';
import { IDonorRepository } from '../../domain/repositories/donor.repository';
import { DonorType } from '../../domain/enums/donor-type.enum';
import { DonationMapper } from '../mappers/donation.mapper';
import { buildDonationDonorEnrichment } from '../mappers/donation-donor-display.helper';
import { EmailTemplateKey } from '../../../../shared/enums/email-template-key';
import { InvoiceEntityType } from '../../../invoice/domain/enums/invoice-entity-type.enum';
import { InvoiceFacade } from '../../../invoice/application/services/invoice.facade';

@Injectable()
@CorrespondenceEventResolver()
export class DonationPaidCorrespondenceResolver
  implements ICorrespondenceEventResolver<DonationPaidEvent> {
  readonly eventType = DonationPaidEvent;
  private readonly logger = new Logger(DonationPaidCorrespondenceResolver.name);

  constructor(
    @Inject(IDonationRepository)
    private readonly donationRepository: IDonationRepository,
    @Inject(IDonorRepository)
    private readonly donorRepository: IDonorRepository,
    private readonly invoiceFacade: InvoiceFacade,
  ) { }

  async resolve(event: DonationPaidEvent): Promise<NotificationSpec[] | null> {
    const donation = await this.donationRepository.findById(event.donationId);
    if (!donation?.donorId) {
      this.logger.warn('No donor for donation ' + donation?.id);
      return null;
    }

    const donor = await this.donorRepository.findById(donation.donorId);
    if (!donor) return null;

    const enrichment = buildDonationDonorEnrichment(donor);
    if (!enrichment?.donorEmail && donor.type === DonorType.GUEST) {
      this.logger.warn('No donor email for donation ' + donation.id);
      return null;
    }

    const donationDto = DonationMapper.toDto(donation, enrichment) as unknown as Record<string, unknown>;
    const donationPeriod =
      donation.startDate && donation.endDate
        ? formatDate(donation.startDate) + ' - ' + formatDate(donation.endDate)
        : 'Not Applicable';
    const paidOn = donation.paidOn ? formatDate(donation.paidOn) : 'Not Applicable';
    const confirmedByName = financeUserFullName(
      donation.confirmedBy as FinanceUserRef | undefined,
    );

    const invoice = await this.invoiceFacade.findIssuedByEntity(
      InvoiceEntityType.DONATION,
      donation.id,
    );
    const attachments: Array<{ filename: string; content: string; contentType?: string }> = [];
    if (invoice?.documentId) {
      const file = await this.invoiceFacade.downloadDocument(invoice.documentId);
      attachments.push({
        filename: file.fileName,
        content: file.buffer.toString('base64'),
        contentType: file.contentType,
      });
    }

    const email = {
      templateKey: EmailTemplateKey.DonationPaid,
      templateData: {
        donation: donationDto,
        donationPeriod,
        paidOn,
        confirmedByName,
        invoiceId: invoice?.id,
      },
      overrideEmails: enrichment?.donorEmail ? [enrichment.donorEmail] : undefined,
      attachments: attachments.length ? attachments : undefined,
    };

    if (donor.type === DonorType.GUEST || !donor.userProfileId) {
      return [{ recipients: { mode: 'users', userIds: [] }, channels: { email } }];
    }

    return [
      {
        recipients: { mode: 'users', userIds: [donor.userProfileId] },
        channels: {
          inApp: {
            title: 'Donation payment confirmed',
            body: `Your donation of ${donation.amount} ${donation.currency} for ${donationPeriod} was marked paid on ${paidOn}.`,
            type: NotificationType.INFO,
            category: 'DONATION',
            priority: NotificationPriority.HIGH,
            referenceId: donation.id,
            referenceType: 'donation',
            metadata: { donation: donationDto },
          },
          email,
          push: { enabled: true },
        },
      },
    ];
  }
}
