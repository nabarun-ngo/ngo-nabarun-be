import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { Donor } from '../../../domain/aggregates/donor/donor.aggregate';
import { DonorNotFoundError, DuplicateDonorEmailError } from '../../../domain/errors/donor.errors';
import { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { UpdateGuestDonorCommand } from './update-guest-donor.command';

@CommandHandler(UpdateGuestDonorCommand)
@Injectable()
export class UpdateGuestDonorHandler implements ICommandHandler<UpdateGuestDonorCommand, Donor> {
  constructor(@Inject(IDonorRepository) private readonly donorRepository: IDonorRepository) {}

  async execute({ params }: UpdateGuestDonorCommand): Promise<Donor> {
    const donor = await this.donorRepository.findById(params.donorId);
    if (!donor) throw new DonorNotFoundError(params.donorId);

    if (params.email && params.email !== donor.email) {
      const existing = await this.donorRepository.findByEmail(params.email);
      if (existing && existing.id !== donor.id) throw new DuplicateDonorEmailError(params.email);
    }

    donor.updateGuestDetails({
      fullName: params.fullName,
      email: params.email,
      phoneCode: params.phoneCode,
      phoneNumber: params.phoneNumber,
    });
    return this.donorRepository.update(donor.id, donor);
  }
}
