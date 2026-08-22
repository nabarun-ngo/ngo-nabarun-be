import { Donor } from '../../domain/aggregates/donor/donor.aggregate';
import { DonorType } from '../../domain/enums/donor-type.enum';
import type { UserInfo } from '@nabarun-ngo/nestjs-shared-core';

export interface DonationDonorEnrichment {
  donorName: string;
  donorEmail?: string;
  donorNumber?: string;
  isGuest: boolean;
  memberUserProfileId?: string;
}

export function buildDonationDonorEnrichment(
  donor: Donor | null | undefined,
  userProfile?: UserInfo | null,
): DonationDonorEnrichment | undefined {
  if (!donor) return undefined;
  if (donor.type === DonorType.GUEST) {
    return {
      donorName: donor.fullName ?? '',
      donorEmail: donor.email,
      donorNumber: donor.phoneNumber,
      isGuest: true,
    };
  }
  const name = userProfile
    ? [userProfile.firstName, userProfile.lastName].filter(Boolean).join(' ').trim()
    : '';
  return {
    donorName: name,
    donorEmail: userProfile?.email,
    donorNumber: userProfile?.phoneNo,
    isGuest: false,
    memberUserProfileId: donor.userProfileId,
  };
}
