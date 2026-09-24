import { Inject, Injectable, Logger } from '@nestjs/common';
import { EventsHandler, IEventHandler } from '@nestjs/cqrs';
import { UserCreatedEvent } from '../../../../../user/domain/events/user-created.event';
import { Donor } from '../../../../domain/aggregates/donor/donor.aggregate';
import { DonorStatus } from '../../../../domain/enums/donor-status.enum';
import { FINANCE_OPTIONS } from '../../../../infrastructure/finance-options.token';
import type { FinanceModuleOptions } from '../../../../finance.schema';
import { IDonorRepository } from '../../../../domain/repositories/donor.repository';

@Injectable()
@EventsHandler(UserCreatedEvent)
export class OnUserCreatedFinanceHandler implements IEventHandler<UserCreatedEvent> {
  private readonly logger = new Logger(OnUserCreatedFinanceHandler.name);

  constructor(
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    @Inject(FINANCE_OPTIONS) private readonly options: FinanceModuleOptions,
  ) {}

  async handle(event: UserCreatedEvent): Promise<void> {
    const existing = await this.donorRepository.findByUserProfileId(event.userId);
    if (existing) {
      if (existing.status === DonorStatus.DELETED) {
        existing.restore();
        await this.donorRepository.update(existing.id, existing);
        this.logger.log(`Restored member donor for user ${event.userId}`);
      }
      return;
    }

    const donor = Donor.createMember({
      userProfileId: event.userId,
      preferredAmount: this.options.defaultDonationAmount,
    });
    await this.donorRepository.create(donor);
    this.logger.log(`Created member donor for user ${event.userId}`);
  }
}
