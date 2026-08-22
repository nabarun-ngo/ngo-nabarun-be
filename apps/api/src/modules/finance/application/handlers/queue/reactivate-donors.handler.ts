import { Inject, Injectable, Logger } from '@nestjs/common';
import { QueueHandler, IQueueHandler, Job } from '@nabarun-ngo/nestjs-shared-queue';
import { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { ReactivateDonorsJob } from './reactivate-donors.job';

@Injectable()
@QueueHandler(ReactivateDonorsJob, { attempts: 3, backoff: { type: 'exponential', delay: 30_000 } })
export class ReactivateDonorsHandler implements IQueueHandler<ReactivateDonorsJob> {
  private readonly logger = new Logger(ReactivateDonorsHandler.name);

  constructor(@Inject(IDonorRepository) private readonly donorRepository: IDonorRepository) {}

  async execute(_job: Job<ReactivateDonorsJob>): Promise<void> {
    const today = new Date();
    today.setHours(23, 59, 59, 999);
    const donors = await this.donorRepository.findDueForReactivation(today);
    for (const donor of donors) {
      donor.activate();
      await this.donorRepository.update(donor.id, donor);
      this.logger.log(`Reactivated donor ${donor.id}`);
    }
  }
}
