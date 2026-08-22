import { DonorStatus } from '../../domain/enums/donor-status.enum';
import { DonorType } from '../../domain/enums/donor-type.enum';
import { Donor } from '../../domain/aggregates/donor/donor.aggregate';
import { DonorDto } from '../dtos/donor.dto';
import type { UserInfo } from '@nabarun-ngo/nestjs-shared-core';

export class DonorMapper {
  static toDto(donor: Donor, userProfile?: UserInfo | null): DonorDto {
    const isGuest = donor.type === DonorType.GUEST;
    const displayName = isGuest
      ? donor.fullName ?? ''
      : userProfile
        ? [userProfile.firstName, userProfile.lastName].filter(Boolean).join(' ').trim()
        : '';

    return {
      id: donor.id,
      type: donor.type,
      status: donor.status,
      preferredAmount: donor.preferredAmount,
      statusEndDate: donor.statusEndDate,
      fullName: isGuest ? donor.fullName : displayName,
      email: isGuest ? donor.email : userProfile?.email,
      phoneCode: isGuest ? donor.phoneCode : undefined,
      phoneNumber: isGuest ? donor.phoneNumber : userProfile?.phoneNo,
      userProfileId: donor.userProfileId,
      createdAt: donor.createdAt,
      updatedAt: donor.updatedAt,
    };
  }
}
