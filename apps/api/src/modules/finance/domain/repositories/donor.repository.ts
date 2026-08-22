import { IRepository } from '@nabarun-ngo/nestjs-shared-core';
import { Donor } from '../aggregates/donor/donor.aggregate';
import { DonorStatus } from '../enums/donor-status.enum';
import { DonorType } from '../enums/donor-type.enum';

export interface DonorFilter {
  id?: string;
  type?: DonorType;
  status?: DonorStatus[];
  userProfileId?: string;
  email?: string;
  q?: string;
  statusEndDate_lte?: Date;
}

export const IDonorRepository = Symbol('IDonorRepository');

export interface IDonorRepository extends IRepository<Donor, string, DonorFilter> {
  findByUserProfileId(userProfileId: string): Promise<Donor | null>;
  findByEmail(email: string): Promise<Donor | null>;
  findScheduleCandidates(donorId?: string): Promise<Donor[]>;
  findDueForReactivation(asOf: Date): Promise<Donor[]>;
  reassignDonations(fromDonorId: string, toDonorId: string): Promise<void>;
}
