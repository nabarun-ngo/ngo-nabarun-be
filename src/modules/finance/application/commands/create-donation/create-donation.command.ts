import { DonationType } from '../../../domain/enums/donation-type.enum';
import { DonationStatus } from '../../../domain/enums/donation-status.enum';

export class CreateDonationCommand {
  constructor(
    public readonly params: {
      type: DonationType;
      amount: number;
      donorId: string;
      startDate?: Date;
      endDate?: Date;
      forEventId?: string;
      initialStatus?: DonationStatus;
      suppressNotification?: boolean;
    },
  ) {}
}
