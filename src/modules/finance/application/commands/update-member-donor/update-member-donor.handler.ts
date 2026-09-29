import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, EventBus, ICommandHandler } from '@nestjs/cqrs';
import { Donor } from '../../../domain/aggregates/donor/donor.aggregate';
import { DonorNotFoundError } from '../../../domain/errors/donor.errors';
import { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { UpdateMemberDonorCommand } from './update-member-donor.command';

@CommandHandler(UpdateMemberDonorCommand)
@Injectable()
export class UpdateMemberDonorHandler implements ICommandHandler<UpdateMemberDonorCommand, Donor> {
  constructor(
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    private readonly eventBus: EventBus,
  ) {}

  async execute({ params }: UpdateMemberDonorCommand): Promise<Donor> {
    const donor = await this.donorRepository.findById(params.donorId);
    if (!donor) throw new DonorNotFoundError(params.donorId);

    donor.updateMemberSchedule({
      preferredAmount: params.preferredAmount,
      status: params.status,
      statusEndDate: params.statusEndDate,
    });

    const saved = await this.donorRepository.update(donor.id, donor);
    const events = [...donor.domainEvents];
    donor.clearEvents();
    this.eventBus.publishAll(events);
    return saved;
  }
}
