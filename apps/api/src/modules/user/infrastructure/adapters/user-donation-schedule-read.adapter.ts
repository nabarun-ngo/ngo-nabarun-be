import { Inject, Injectable } from '@nestjs/common';
import { IUserRepository } from '../../domain/repositories/user.repository';
import { UserStatus } from '../../domain/enums/user-status.enum';
import {
  DonationScheduleUserReadModel,
  IUserDonationScheduleReadPort,
} from '../../domain/ports/user-donation-schedule-read.port';

@Injectable()
export class UserDonationScheduleReadAdapter implements IUserDonationScheduleReadPort {
  constructor(@Inject(IUserRepository) private readonly userRepository: IUserRepository) {}

  async findActiveDonors(userId?: string): Promise<DonationScheduleUserReadModel[]> {
    if (userId) {
      const user = await this.userRepository.findById(userId);
      return user ? [this.toReadModel(user)] : [];
    }

    const users = await this.userRepository.findAll({ status: UserStatus.ACTIVE });
    return users.map((u) => this.toReadModel(u));
  }

  private toReadModel(user: {
    id: string;
    fullName: string;
    donationAmount?: number;
    donationPauseStart?: Date;
    donationPauseEnd?: Date;
  }): DonationScheduleUserReadModel {
    return {
      id: user.id,
      fullName: user.fullName,
      donationAmount: user.donationAmount,
      donationPauseStart: user.donationPauseStart,
      donationPauseEnd: user.donationPauseEnd,
    };
  }
}
