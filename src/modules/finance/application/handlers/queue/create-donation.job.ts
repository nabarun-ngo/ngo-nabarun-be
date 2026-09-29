import { DonationStatus } from '../../../domain/enums/donation-status.enum';

export class CreateDonationJob {
  constructor(
    public readonly payload: {
      donorId: string;
      amount: number;
      firstDate: string;
      lastDate: string;
      initialStatus?: DonationStatus;
      suppressNotification?: boolean;
    },
  ) {}
}
