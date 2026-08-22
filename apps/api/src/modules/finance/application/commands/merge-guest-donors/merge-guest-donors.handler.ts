import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, EventBus, ICommandHandler } from '@nestjs/cqrs';
import { Donor } from '../../../domain/aggregates/donor/donor.aggregate';
import { DonorType } from '../../../domain/enums/donor-type.enum';
import { DonorStatus } from '../../../domain/enums/donor-status.enum';
import { DonorMergedEvent } from '../../../domain/events/donor-merged.event';
import { DonorNotFoundError, InvalidDonorMergeError } from '../../../domain/errors/donor.errors';
import { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { MergeGuestDonorsCommand } from './merge-guest-donors.command';

@CommandHandler(MergeGuestDonorsCommand)
@Injectable()
export class MergeGuestDonorsHandler implements ICommandHandler<MergeGuestDonorsCommand, Donor> {
  constructor(
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    private readonly eventBus: EventBus,
  ) {}

  async execute({ params }: MergeGuestDonorsCommand): Promise<Donor> {
    if (params.sourceDonorId === params.targetDonorId) {
      throw new InvalidDonorMergeError('Source and target donor must be different');
    }

    const [source, target] = await Promise.all([
      this.donorRepository.findById(params.sourceDonorId),
      this.donorRepository.findById(params.targetDonorId),
    ]);
    if (!source) throw new DonorNotFoundError(params.sourceDonorId);
    if (!target) throw new DonorNotFoundError(params.targetDonorId);

    if (source.type !== DonorType.GUEST || target.type !== DonorType.GUEST) {
      throw new InvalidDonorMergeError('Both donors must be guest type');
    }
    if (source.status === DonorStatus.DELETED || target.status === DonorStatus.DELETED) {
      throw new InvalidDonorMergeError('Cannot merge deleted donors');
    }

    target.absorbGuestContactFrom(source);
    await this.donorRepository.reassignDonations(source.id, target.id);
    source.markDeleted();
    await this.donorRepository.update(source.id, source);
    const saved = await this.donorRepository.update(target.id, target);

    this.eventBus.publish(new DonorMergedEvent(source.id, target.id));
    return saved;
  }
}
