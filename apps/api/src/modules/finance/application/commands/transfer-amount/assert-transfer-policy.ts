import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { Account } from '../../../domain/aggregates/account/account.aggregate';
import { AccountOwnerType } from '../../../domain/enums/account-owner-type.enum';
import { AccountStatus } from '../../../domain/enums/account-status.enum';
import { AccountType } from '../../../domain/enums/account-type.enum';
import type { TransferMatrixRow } from '../../ports/finance-reference-data.port';
import { DEFAULT_TRANSFER_MATRIX, findTransferMatrixRow } from './transfer-matrix';

export type TransferReference = 'ADHOC' | 'ADVANCE_EV';

export function assertTransferPolicy(
  fromAccount: Account,
  toAccount: Account,
  reference: TransferReference,
  actorUserId?: string,
  matrix: readonly TransferMatrixRow[] = DEFAULT_TRANSFER_MATRIX,
): void {
  if (fromAccount.id === toAccount.id) {
    throw new BusinessException('Cannot transfer to the same account');
  }

  if (fromAccount.status === AccountStatus.CLOSED || toAccount.status === AccountStatus.CLOSED) {
    throw new BusinessException('Cannot transfer involving a closed account');
  }

  if (fromAccount.type === AccountType.INVESTMENT || toAccount.type === AccountType.INVESTMENT) {
    throw new BusinessException('Investment accounts cannot be part of a transfer');
  }

  if (fromAccount.ownerType === AccountOwnerType.ORG) {
    if (!actorUserId || !fromAccount.custodianUserIds.includes(actorUserId)) {
      throw new BusinessException('Only custodians can transfer from an organization account');
    }
  }

  const row = findTransferMatrixRow(matrix, fromAccount.type, reference);
  if (!row) {
    if (fromAccount.type === AccountType.WALLET && reference !== 'ADHOC') {
      throw new BusinessException('Wallet transfers only support General (ADHOC) reference');
    }
    throw new BusinessException('Unsupported transfer source account type');
  }

  if (!row.toAccountTypes.includes(toAccount.type)) {
    if (fromAccount.type === AccountType.WALLET) {
      throw new BusinessException('Wallet surplus can only be transferred to a bank account');
    }
    if (reference === 'ADVANCE_EV') {
      throw new BusinessException('Advance for Event can only transfer to a wallet');
    }
    if (reference === 'ADHOC') {
      throw new BusinessException('General transfer from bank must target a bank or wallet');
    }
    throw new BusinessException('Unsupported transfer destination account type');
  }
}
