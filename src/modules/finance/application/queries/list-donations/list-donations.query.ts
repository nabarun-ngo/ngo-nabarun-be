import { DonationStatus } from '../../../domain/enums/donation-status.enum';
import { DonationType } from '../../../domain/enums/donation-type.enum';
import { DonorType } from '../../../domain/enums/donor-type.enum';

export class ListDonationsQuery {
  constructor(
    public readonly filter: {
      donationId?: string;
      donorId?: string;
      userProfileId?: string;
      status?: DonationStatus[];
      type?: DonationType[];
      donorType?: DonorType;
      isGuest?: 'Y' | 'N';
      forEventId?: string;
      startDate?: Date;
      endDate?: Date;
      pageIndex?: number;
      pageSize?: number;
    } = {},
  ) {}
}
