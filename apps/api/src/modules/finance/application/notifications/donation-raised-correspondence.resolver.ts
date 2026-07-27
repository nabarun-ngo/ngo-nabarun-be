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
import { DonationMapper } from '../mappers/donation.mapper';
import { EmailTemplateKey } from '../../../../shared/email-template-key';

/**
 * Resolves DonationRaisedEvent to a donor notification. Enriches the thin domain
 * event via the donation repository, then builds the display-ready spec (wording,
 * template key, channels) — the mapping that previously lived in the correspondence
 * notification-policy registry.
 */
@Injectable()
@CorrespondenceEventResolver()
export class DonationRaisedCorrespondenceResolver
  implements ICorrespondenceEventResolver<DonationRaisedEvent>
{
  readonly eventType = DonationRaisedEvent;
  private readonly logger = new Logger(DonationRaisedCorrespondenceResolver.name);

  constructor(
    @Inject(IDonationRepository)
    private readonly donationRepository: IDonationRepository,
  ) {}

  async resolve(event: DonationRaisedEvent): Promise<NotificationSpec[] | null> {
    const donation = await this.donationRepository.findById(event.donationId);
    if (!donation?.donorEmail) {
      this.logger.warn('No donor email for donation ' + donation?.id);
      return null;
    }

    const donationDto = DonationMapper.toDto(donation) as unknown as Record<string, unknown>;
    const donationPeriod =
      donation.startDate && donation.endDate
        ? formatDate(donation.startDate) + ' - ' + formatDate(donation.endDate)
        : 'Not Applicable';

    const email = {
      templateKey: EmailTemplateKey.DonationCreated,
      templateData: { donation: donationDto, donationPeriod },
      overrideEmails: donation.donorEmail ? [donation.donorEmail] : undefined,
    };

    if (!donation.donorId) {
      return [{ recipients: { mode: 'users', userIds: [] }, channels: { email } }];
    }

    return [
      {
        recipients: { mode: 'users', userIds: [donation.donorId] },
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
