import { Inject, Injectable } from '@nestjs/common';
import { QueueHandler, IQueueHandler, Job } from '@nabarun-ngo/nestjs-shared-queue';
import { formatDate, IUserLookupPort } from '@nabarun-ngo/nestjs-shared-core';
import { CorrespondenceFacade } from '@nabarun-ngo/nestjs-shared-correspondence';
import { DonationStatus } from '../../../domain/enums/donation-status.enum';
import { DonorType } from '../../../domain/enums/donor-type.enum';
import { IDonationRepository } from '../../../domain/repositories/donation.repository';
import { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { displayForDonation, enrichDonationsForReport } from '../../reports/donation-report-enrichment.helper';
import { RemindPendingDonationsJob } from './remind-pending-donations.job';
import { EmailTemplateKey } from '../../../../../shared/enums/email-template-key';

@Injectable()
@QueueHandler(RemindPendingDonationsJob, { attempts: 3, backoff: { type: 'exponential', delay: 30_000 } })
export class RemindPendingDonationsHandler implements IQueueHandler<RemindPendingDonationsJob> {
  constructor(
    @Inject(IDonationRepository) private readonly donationRepository: IDonationRepository,
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    @Inject(IUserLookupPort) private readonly userLookup: IUserLookupPort,
    private readonly correspondence: CorrespondenceFacade,
  ) { }

  async execute(job: Job<RemindPendingDonationsJob>): Promise<void> {
    const donorIdFilter = job.data.payload?.donorId;
    const donations = await this.donationRepository.findAll({
      status: [DonationStatus.PENDING],
      donorType: DonorType.MEMBER,
      donorId: donorIdFilter,
    });

    const displays = await enrichDonationsForReport(donations, this.donorRepository, this.userLookup);
    const byDonor = new Map<string, typeof donations>();
    for (const d of donations) {
      if (!d.donorId) continue;
      const list = byDonor.get(d.donorId) ?? [];
      list.push(d);
      byDonor.set(d.donorId, list);
    }

    for (const [donorId, pending] of byDonor) {
      const display = displayForDonation(pending[0], displays);
      const donor = await this.donorRepository.findById(donorId);
      if (!display.donorEmail && !donor?.userProfileId) continue;

      await this.correspondence.dispatch({
        recipients: { mode: 'users', userIds: donor?.userProfileId ? [donor.userProfileId] : [] },
        channels: {
          email: {
            templateKey: EmailTemplateKey.DonationReminder,
            templateData: {
              donorName: display.donorName,
              donations: pending.map((d) => ({
                id: d.id,
                period: formatDate(d.startDate!) + ' - ' + formatDate(d.endDate!),
                amount: d.amount,
              })),
            },
            overrideEmails: display.donorEmail ? [display.donorEmail] : undefined,
          },
        },
      });
      job.log?.('Reminder sent to donor ' + donorId);
    }
  }
}
