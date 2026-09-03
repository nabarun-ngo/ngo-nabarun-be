import { DonorStatus } from '../../domain/enums/donor-status.enum';
import { DonorType } from '../../domain/enums/donor-type.enum';
import { KeyValueOption } from '../ports/finance-reference-data.port';

export class DonorRefDataDto {
  donorStatuses?: KeyValueOption[];
  memberEditableDonorStatuses?: KeyValueOption[];
  statusesRequiringEndDate?: string[];
}

export class DonorDto {
  id!: string;
  type!: DonorType;
  status!: DonorStatus;
  preferredAmount?: number;
  statusEndDate?: Date;
  fullName?: string;
  email?: string;
  phoneCode?: string;
  phoneNumber?: string;
  userProfileId?: string;
  createdAt!: Date;
  updatedAt!: Date;
}

export class MergeGuestDonorsDto {
  sourceDonorId!: string;
  targetDonorId!: string;
}
