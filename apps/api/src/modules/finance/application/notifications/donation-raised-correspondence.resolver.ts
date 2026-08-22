import { Inject, Injectable, Logger } from '@nestjs/common';
import { formatDate } from '@nabarun-ngo/nestjs-shared-core';
import {
  ICorrespondenceEventResolver,
  CorrespondenceEventResolver,
  NotificationSpec,
  NotificationType,
  NotificationPriority,
} from '@nabarun-ngo/nestjs-shared-correspondence';
import { DonationRaisedEvent } from '../../domain/events/donation-raised.event';
import { IDonationRepository } from '../../domain/repositories/donation.repository';
import { IDonorRepository } from '../../domain/repositories/donor.repository';
import { DonorType } from '../../domain/enums/donor-type.enum';
import { DonationMapper } from '../mappers/donation.mapper';
import { buildDonationDonorEnrichment } from '../mappers/donation-donor-display.helper';
import { EmailTemplateKey } from '../../../../shared/enums/email-template-key';

@Injectable()
@CorrespondenceEventResolver()
export class DonationRaisedCorrespondenceResolver
  implements ICorrespondenceEventResolver<DonationRaisedEvent> {
  readonly eventType = DonationRaisedEvent;
  private readonly logger = new Logger(DonationRaisedCorrespondenceResolver.name);

  constructor(
    @Inject(IDonationRepository)
    private readonly donationRepository: IDonationRepository,
    @Inject(IDonorRepository)
    private readonly donorRepository: IDonorRepository,
  ) { }

  async resolve(event: DonationRaisedEvent): Promise<NotificationSpec[] | null> {
    const donation = await this.donationRepository.findById(event.donationId);
    if (!donation?.donorId) {
      this.logger.warn('No donor for donation ' + donation?.id);
      return null;
    }

    const donor = await this.donorRepository.findById(donation.donorId);
    if (!donor) return null;

    const enrichment = buildDonationDonorEnrichment(donor);
    const donationDto = DonationMapper.toDto(donation, enrichment) as unknown as Record<string, unknown>;
    const donationPeriod =
      donation.startDate && donation.endDate
        ? formatDate(donation.startDate) + ' - ' + formatDate(donation.endDate)
        : 'Not Applicable';

    const email = {
      templateKey: EmailTemplateKey.DonationCreated,
      templateData: { donation: donationDto, donationPeriod },
      overrideEmails: enrichment?.donorEmail ? [enrichment.donorEmail] : undefined,
    };

    if (donor.type === DonorType.GUEST || !donor.userProfileId) {
      if (!enrichment?.donorEmail) {
        this.logger.warn('No donor email for guest donation ' + donation.id);
        return null;
      }
      return [{ recipients: { mode: 'users', userIds: [] }, channels: { email } }];
    }

    return [
      {
        recipients: { mode: 'users', userIds: [donor.userProfileId] },
        channels: {
          inApp: {
            title: 'Donation raised',
            body: `Your donation of ${donation.amount} ${donation.currency} has been raised for ${donationPeriod}.`,
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
