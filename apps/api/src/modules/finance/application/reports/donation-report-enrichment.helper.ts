import { IUserLookupPort } from '@nabarun-ngo/nestjs-shared-core';
import { Donation } from '../../domain/aggregates/donation/donation.aggregate';
import { DonorType } from '../../domain/enums/donor-type.enum';
import { IDonorRepository } from '../../domain/repositories/donor.repository';

export type DonationReportDonorDisplay = {
  donorId?: string;
  donorName: string;
  donorEmail?: string;
  donorPhone?: string;
  isGuest: boolean;
};

export async function enrichDonationsForReport(
  donations: Donation[],
  donorRepository: IDonorRepository,
  userLookup: IUserLookupPort,
): Promise<Map<string, DonationReportDonorDisplay>> {
  const donorIds = [...new Set(donations.map((d) => d.donorId).filter(Boolean))] as string[];
  const donors = await Promise.all(donorIds.map((id) => donorRepository.findById(id)));
  const memberProfileIds = donors
    .filter((d) => d?.type === DonorType.MEMBER && d.userProfileId)
    .map((d) => d!.userProfileId!);
  const users = memberProfileIds.length > 0 ? await userLookup.findByIds(memberProfileIds) : [];
  const userMap = new Map(users.map((u) => [u.id, u]));

  const result = new Map<string, DonationReportDonorDisplay>();
  for (const donor of donors) {
    if (!donor) continue;
    if (donor.type === DonorType.GUEST) {
      result.set(donor.id, {
        donorId: donor.id,
        donorName: donor.fullName ?? 'Guest',
        donorEmail: donor.email,
        donorPhone: donor.phoneNumber,
        isGuest: true,
      });
    } else {
      const user = donor.userProfileId ? userMap.get(donor.userProfileId) : undefined;
      result.set(donor.id, {
        donorId: donor.id,
        donorName: user
          ? [user.firstName, user.lastName].filter(Boolean).join(' ').trim()
          : donor.userProfileId ?? donor.id,
        donorEmail: user?.email,
        donorPhone: user?.phoneNo,
        isGuest: false,
      });
    }
  }
  return result;
}

export function displayForDonation(
  donation: Donation,
  displays: Map<string, DonationReportDonorDisplay>,
): DonationReportDonorDisplay {
  if (!donation.donorId) {
    return { donorName: 'Unknown', isGuest: false };
  }
  return displays.get(donation.donorId) ?? {
    donorId: donation.donorId,
    donorName: donation.donorId,
    isGuest: false,
  };
}
