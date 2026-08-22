import { AccountStatus } from '../../../domain/enums/account-status.enum';
import { AccountType } from '../../../domain/enums/account-type.enum';
import { AccountOwnerType } from '../../../domain/enums/account-owner-type.enum';

export class ListAccountsQuery {
  constructor(
    public readonly filter: {
      accountId?: string;
      accountHolderId?: string;
      status?: AccountStatus[];
      type?: AccountType[];
      ownerType?: AccountOwnerType[];
      includePaymentDetail?: 'Y' | 'N';
      includeBalance?: 'Y' | 'N';
    } = {},
    public readonly pageIndex?: number,
    public readonly pageSize?: number,
    public readonly userId?: string,
  ) {}
}

