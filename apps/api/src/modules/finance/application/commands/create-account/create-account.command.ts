import { AccountOwnerType } from '../../../domain/enums/account-owner-type.enum';
import { AccountType } from '../../../domain/enums/account-type.enum';

export class CreateAccountCommand {
  constructor(
    public readonly params: {
      name: string;
      type: AccountType;
      ownerType: AccountOwnerType;
      currency: string;
      description?: string;
      accountHolderId?: string;
      /** @deprecated Use custodianUserIds */
      custodianUserId?: string;
      custodianUserIds?: string[];
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
      upiDetails?: Array<{
        id?: string;
        payeeName?: string;
        upiId?: string;
        mobileNumber?: string;
        qrData?: string;
        label?: string;
        isPrimary?: boolean;
      }>;
    },
  ) {}
}
