export const IUserDonationScheduleReadPort = Symbol('IUserDonationScheduleReadPort');

export interface DonationScheduleUserReadModel {
  id: string;
  fullName: string;
  donationAmount?: number;
  donationPauseStart?: Date;
  donationPauseEnd?: Date;
}

/**
 * Read-only port for finance scheduled donation jobs.
 * Owned by UserModule — finance imports UserModule only (acyclic).
 */
export interface IUserDonationScheduleReadPort {
  findActiveDonors(userId?: string): Promise<DonationScheduleUserReadModel[]>;
}
