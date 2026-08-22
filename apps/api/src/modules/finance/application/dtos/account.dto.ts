import { AccountStatus } from '../../domain/enums/account-status.enum';
import { AccountOwnerType } from '../../domain/enums/account-owner-type.enum';
import { AccountType } from '../../domain/enums/account-type.enum';
import { KeyValueOption } from '../ports/finance-reference-data.port';

export class BankDetailDto {
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
}

export class UPIDetailDto {
  id?: string;
  payeeName?: string;
  upiId?: string;
  mobileNumber?: string;
  qrData?: string;
  label?: string;
  isPrimary?: boolean;
}

export class AccountDetailDto {
  id!: string;
  accountHolderName?: string;
  accountHolder?: string;
  ownerType?: AccountOwnerType;
  /** @deprecated Use custodianUserIds */
  custodianUserId?: string;
  custodianUserIds?: string[];
  balance?: number;
  accountStatus!: AccountStatus;
  activatedOn?: Date;
  accountType!: AccountType;
  bankDetail?: BankDetailDto;
  /** @deprecated Use upiDetails */
  upiDetail?: UPIDetailDto;
  upiDetails?: UPIDetailDto[];
}

export class AccountRefDataDto {
  accountStatuses?: KeyValueOption[];
  accountTypes?: KeyValueOption[];
  ownerTypes?: KeyValueOption[];
  bankAccountTypes?: KeyValueOption[];
  investmentTypes?: KeyValueOption[];
  interestPayingTerms?: KeyValueOption[];
  transferReferenceTypes?: KeyValueOption[];
  transferMatrix?: Array<{
    fromAccountType: string;
    reference: string;
    toAccountTypes: string[];
  }>;
  transactionTypes?: KeyValueOption[];
  transactionStatuses?: KeyValueOption[];
  transactionRefTypes?: KeyValueOption[];
  accountStatusGroups?: {
    outstanding: string[];
    closed: string[];
    excluded: string[];
  };
}
