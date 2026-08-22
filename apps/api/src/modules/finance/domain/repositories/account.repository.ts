import { IRepository } from '@nabarun-ngo/nestjs-shared-core';
import { Account } from '../aggregates/account/account.aggregate';
import { AccountOwnerType } from '../enums/account-owner-type.enum';
import { AccountStatus } from '../enums/account-status.enum';
import { AccountType } from '../enums/account-type.enum';

export interface AccountFilter {
  id?: string;
  type?: AccountType[];
  ownerType?: AccountOwnerType[];
  status?: AccountStatus[];
  accountHolderName?: string;
  accountHolderId?: string | null;
  /** Investments funded from this bank account. */
  sourceAccountId?: string;
  includeBalance?: boolean;
}

export const IAccountRepository = Symbol('IAccountRepository');

export interface IAccountRepository extends IRepository<Account, string, AccountFilter> {
  update(id: string, account: Account, options?: { replaceUpiDetails?: boolean }): Promise<Account>;
}
