import { Inject, Injectable, Logger } from '@nestjs/common';
import { QueueHandler, IQueueHandler, Job, JobExecutionContext } from '@nabarun-ngo/nestjs-shared-queue';
import { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { FINANCE_OPTIONS } from '../../../infrastructure/finance-options.token';
import type { FinanceModuleOptions } from '../../../finance.schema';
import { DonationStatus } from '../../../domain/enums/donation-status.enum';
import { TriggerMonthlyDonationJob } from './trigger-monthly-donation.job';

@Injectable()
@QueueHandler(TriggerMonthlyDonationJob, { attempts: 3, backoff: { type: 'exponential', delay: 30_000 } })
export class TriggerMonthlyDonationHandler implements IQueueHandler<TriggerMonthlyDonationJob> {
  private readonly logger = new Logger(TriggerMonthlyDonationHandler.name);

  constructor(
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    @Inject(FINANCE_OPTIONS) private readonly options: FinanceModuleOptions,
  ) { }

  async execute(job: Job<TriggerMonthlyDonationJob>, ctx: JobExecutionContext): Promise<void> {
    const donorId = job.data.payload?.donorId;
    const donors = await this.donorRepository.findScheduleCandidates(donorId);

    const today = new Date();
    const firstDate = new Date(today.getFullYear(), today.getMonth(), 1);
    const lastDate = new Date(today.getFullYear(), today.getMonth() + 1, 0);
    const defaultAmount = this.options.defaultDonationAmount;

    for (const donor of donors) {
      if (!donor.shouldRaiseDonation()) {
        job.log?.(`Skipping donor ${donor.id} with status ${donor.status}`);
        continue;
      }

      const amount = donor.preferredAmount && donor.preferredAmount > 0
        ? donor.preferredAmount
        : defaultAmount;

      const isPaused = !donor.shouldNotifyOnRaise();
      ctx.addChildJob('CreateDonationJob', {
        payload: {
          donorId: donor.id,
          amount,
          firstDate: firstDate.toISOString(),
          lastDate: lastDate.toISOString(),
          initialStatus: isPaused ? DonationStatus.PAY_LATER : DonationStatus.RAISED,
          suppressNotification: isPaused,
        },
      });
    }
  }
}
