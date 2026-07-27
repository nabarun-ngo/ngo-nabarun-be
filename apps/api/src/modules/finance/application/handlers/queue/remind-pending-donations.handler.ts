import { Inject, Injectable } from '@nestjs/common';
import { QueueHandler, IQueueHandler, Job } from '@nabarun-ngo/nestjs-shared-queue';
import { formatDate } from '@nabarun-ngo/nestjs-shared-core';
import { CorrespondenceFacade } from '@nabarun-ngo/nestjs-shared-correspondence';
import { DonationStatus } from '../../../domain/enums/donation-status.enum';
import { IDonationRepository } from '../../../domain/repositories/donation.repository';
import { RemindPendingDonationsJob } from './remind-pending-donations.job';
import { EmailTemplateKey } from '../../../../../shared/email-template-key';

@Injectable()
@QueueHandler(RemindPendingDonationsJob, { attempts: 3, backoff: { type: 'exponential', delay: 30_000 } })
export class RemindPendingDonationsHandler implements IQueueHandler<RemindPendingDonationsJob> {
  constructor(
    @Inject(IDonationRepository) private readonly donationRepository: IDonationRepository,
    private readonly correspondence: CorrespondenceFacade,
  ) { }

  async execute(job: Job<RemindPendingDonationsJob>): Promise<void> {
    const userId = job.data.payload?.userId;
    const donations = await this.donationRepository.findAll({
      status: [DonationStatus.PENDING],
      isGuest: false,
      donorId: userId,
    });

    const byUser = new Map<string, typeof donations>();
    for (const d of donations) {
      if (!d.donorId) continue;
      const list = byUser.get(d.donorId) ?? [];
      list.push(d);
      byUser.set(d.donorId, list);
    }

    for (const [donorId, pending] of byUser) {
      if (!pending[0]?.donorEmail) continue;
      await this.correspondence.dispatch({
        recipients: { mode: 'users', userIds: [donorId] },
        channels: {
          email: {
            templateKey: EmailTemplateKey.DonationReminder,
            templateData: {
              donorName: pending[0].donorName,
              donations: pending.map((d) => ({
                id: d.id,
                period: formatDate(d.startDate!) + ' - ' + formatDate(d.endDate!),
                amount: d.amount,
              })),
            },
            overrideEmails: [pending[0].donorEmail],
          },
        },
      });
      job.log?.('Reminder sent to ' + donorId);
    }
  }
}
