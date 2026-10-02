import { DonorStatus } from '../../../domain/enums/donor-status.enum';

export class UpdateMemberDonorCommand {
  constructor(
    public readonly params: {
      donorId: string;
      preferredAmount?: number;
      status?: DonorStatus;
      statusEndDate?: Date;
    },
  ) {}
}
