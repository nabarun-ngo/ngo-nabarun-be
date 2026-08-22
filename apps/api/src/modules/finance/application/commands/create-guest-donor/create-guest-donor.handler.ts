import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, EventBus, ICommandHandler } from '@nestjs/cqrs';
import { Donor } from '../../../domain/aggregates/donor/donor.aggregate';
import { DuplicateDonorEmailError } from '../../../domain/errors/donor.errors';
import { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { CreateGuestDonorCommand } from './create-guest-donor.command';

@CommandHandler(CreateGuestDonorCommand)
@Injectable()
export class CreateGuestDonorHandler implements ICommandHandler<CreateGuestDonorCommand, Donor> {
  constructor(
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    private readonly eventBus: EventBus,
  ) {}

  async execute({ params }: CreateGuestDonorCommand): Promise<Donor> {
    if (params.email) {
      const existing = await this.donorRepository.findByEmail(params.email);
      if (existing) throw new DuplicateDonorEmailError(params.email);
    }

    const donor = Donor.createGuest(params);
    const saved = await this.donorRepository.create(donor);
    const events = [...donor.domainEvents];
    donor.clearEvents();
    this.eventBus.publishAll(events);
    return saved;
  }
}
