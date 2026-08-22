import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, EventBus, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { Donation } from '../../../domain/aggregates/donation/donation.aggregate';
import { DonationStatus } from '../../../domain/enums/donation-status.enum';
import { DonationType } from '../../../domain/enums/donation-type.enum';
import { DonorNotFoundError } from '../../../domain/errors/donor.errors';
import { IDonationRepository } from '../../../domain/repositories/donation.repository';
import { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { CreateDonationCommand } from './create-donation.command';

@CommandHandler(CreateDonationCommand)
@Injectable()
export class CreateDonationHandler implements ICommandHandler<CreateDonationCommand, Donation> {
  constructor(
    @Inject(IDonationRepository) private readonly donationRepository: IDonationRepository,
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    private readonly eventBus: EventBus,
  ) { }

  async execute({ params: request }: CreateDonationCommand): Promise<Donation> {
    const donor = await this.donorRepository.findById(request.donorId);
    if (!donor) throw new DonorNotFoundError(request.donorId);

    const donation = Donation.create({
      type: request.type,
      amount: request.amount,
      donorId: request.donorId,
      startDate: request.startDate,
      endDate: request.endDate,
      initialStatus: request.initialStatus,
      suppressNotification: request.suppressNotification,
    });

    if (request.forEventId) {
      donation.update({ forEventId: request.forEventId });
    }

    if (request.type === DonationType.REGULAR) {
      const donations = await this.donationRepository.findAll({
        type: [DonationType.REGULAR],
        donorId: request.donorId,
        startDate_lte: request.endDate,
        endDate_gte: request.startDate,
        status: [...Donation.outstandingStatus, DonationStatus.PAID],
      });
      if (donations.length > 0) {
        throw new BusinessException('Donation already exists for this donor in the given period');
      }
    }

    const saved = await this.donationRepository.create(donation);
    const events = [...donation.domainEvents];
    donation.clearEvents();
    this.eventBus.publishAll(events);
    return saved;
  }
}
