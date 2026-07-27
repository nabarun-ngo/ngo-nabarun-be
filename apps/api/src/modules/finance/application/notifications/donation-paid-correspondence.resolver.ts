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
import { DonationMapper } from '../mappers/donation.mapper';
import { EmailTemplateKey } from '../../../../shared/enums/email-template-key';

/**
 * Resolves DonationPaidEvent to a payment-confirmed donor notification. Enriches
 * the thin domain event via the donation repository, then builds the display-ready
 * spec that previously lived in the correspondence notification-policy registry.
 */
@Injectable()
@CorrespondenceEventResolver()
export class DonationPaidCorrespondenceResolver
  implements ICorrespondenceEventResolver<DonationPaidEvent> {
  readonly eventType = DonationPaidEvent;
  private readonly logger = new Logger(DonationPaidCorrespondenceResolver.name);

  constructor(
    @Inject(IDonationRepository)
    private readonly donationRepository: IDonationRepository,
  ) { }

  async resolve(event: DonationPaidEvent): Promise<NotificationSpec[] | null> {
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
    const paidOn = donation.paidOn ? formatDate(donation.paidOn) : 'Not Applicable';
    const confirmedByName = financeUserFullName(
      donation.confirmedBy as FinanceUserRef | undefined,
    );

    const email = {
      templateKey: EmailTemplateKey.DonationPaid,
      templateData: { donation: donationDto, donationPeriod, paidOn, confirmedByName },
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
