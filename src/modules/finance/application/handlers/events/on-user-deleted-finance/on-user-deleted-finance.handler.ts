import { Inject, Injectable, Logger } from '@nestjs/common';
import { EventsHandler, IEventHandler } from '@nestjs/cqrs';
import { UserDeletedEvent } from '../../../../../user/domain/events/user-deleted.event';
import { Donation } from '../../../../domain/aggregates/donation/donation.aggregate';
import { IDonationRepository } from '../../../../domain/repositories/donation.repository';
import { IDonorRepository } from '../../../../domain/repositories/donor.repository';

@Injectable()
@EventsHandler(UserDeletedEvent)
export class OnUserDeletedFinanceHandler implements IEventHandler<UserDeletedEvent> {
  private readonly logger = new Logger(OnUserDeletedFinanceHandler.name);

  constructor(
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    @Inject(IDonationRepository) private readonly donationRepository: IDonationRepository,
  ) {}

  async handle(event: UserDeletedEvent): Promise<void> {
    const donor = await this.donorRepository.findByUserProfileId(event.userId);
    if (!donor) {
      this.logger.warn(`No member donor found for deleted user ${event.userId}`);
      return;
    }

    donor.markDeleted();
    await this.donorRepository.update(donor.id, donor);

    const donations = await this.donationRepository.findAll({
      donorId: donor.id,
      status: Donation.outstandingStatus,
    });
    for (const donation of donations) {
      donation.cancel();
      await this.donationRepository.update(donation.id, donation);
      this.logger.debug('Cancelled donation ' + donation.id);
    }
  }
}
