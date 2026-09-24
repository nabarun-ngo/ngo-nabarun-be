import { AccountStatus } from '../../../domain/enums/account-status.enum';

export class UpdateAccountCommand {
  constructor(
    public readonly params: {
      id: string;
      name?: string;
      description?: string;
      accountStatus?: AccountStatus;
      bankDetail?: {
        bankAccountHolderName?: string;
        bankName?: string;
        bankBranch?: string;
        bankAccountNumber?: string;
        bankAccountType?: string;
        IFSCNumber?: string;
        maturityDate?: string;
        maturityAmount?: number;
        investmentAmount?: number;
        sourceAccountId?: string;
        dematId?: string;
        interestRate?: number;
        interestPayingTerm?: string;
      };
      upiDetail?: {
        id?: string;
        payeeName?: string;
        upiId?: string;
        mobileNumber?: string;
        qrData?: string;
        label?: string;
        isPrimary?: boolean;
      };
      upiDetails?: Array<{
        id?: string;
        payeeName?: string;
        upiId?: string;
        mobileNumber?: string;
        qrData?: string;
        label?: string;
        isPrimary?: boolean;
      }>;
      actorUserId?: string;
    },
  ) {}
}
