import { AccountOwnerType } from '../enums/account-owner-type.enum';
import { AccountType } from '../enums/account-type.enum';

export const ORG_ACCOUNT_DISPLAY_NAME = 'Nabarun';

export const MAX_UPI_DETAILS_PER_ACCOUNT = 10;

export type AccountDetailField = 'bankDetail' | 'upiDetails';

export interface AccountTypeConfig {
  allowedOwnerTypes: AccountOwnerType[];
  requiredDetails: AccountDetailField[];
  optionalDetails: AccountDetailField[];
}

export const ACCOUNT_TYPE_CONFIG: Record<AccountType, AccountTypeConfig> = {
  [AccountType.BANK]: {
    allowedOwnerTypes: [AccountOwnerType.ORG, AccountOwnerType.INDIVIDUAL],
    requiredDetails: ['bankDetail'],
    optionalDetails: ['upiDetails'],
  },
  [AccountType.INVESTMENT]: {
    allowedOwnerTypes: [AccountOwnerType.ORG, AccountOwnerType.INDIVIDUAL],
    requiredDetails: ['bankDetail'],
    optionalDetails: [],
  },
  [AccountType.WALLET]: {
    allowedOwnerTypes: [AccountOwnerType.INDIVIDUAL],
    requiredDetails: [],
    optionalDetails: ['bankDetail', 'upiDetails'],
  },
};

export function isOwnerTypeAllowed(type: AccountType, ownerType: AccountOwnerType): boolean {
  return ACCOUNT_TYPE_CONFIG[type].allowedOwnerTypes.includes(ownerType);
}
