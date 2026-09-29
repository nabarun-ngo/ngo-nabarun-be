import type { TransferMatrixRow } from '../../ports/finance-reference-data.port';

/** Default matrix — must stay aligned with transfer-matrix.json seed. */
export const DEFAULT_TRANSFER_MATRIX: TransferMatrixRow[] = [
  { fromAccountType: 'WALLET', reference: 'ADHOC', toAccountTypes: ['BANK'] },
  { fromAccountType: 'BANK', reference: 'ADHOC', toAccountTypes: ['BANK', 'WALLET'] },
  { fromAccountType: 'BANK', reference: 'ADVANCE_EV', toAccountTypes: ['WALLET'] },
];

export function findTransferMatrixRow(
  matrix: readonly TransferMatrixRow[],
  fromAccountType: string,
  reference: string,
): TransferMatrixRow | undefined {
  return matrix.find(
    (row) => row.fromAccountType === fromAccountType && row.reference === reference,
  );
}

export function resolveTransferToAccountTypes(
  matrix: readonly TransferMatrixRow[],
  fromAccountType: string | undefined,
  reference: string,
): string[] | null {
  if (!fromAccountType) {
    // No from account: BANK+ADHOC defaults (legacy payable list without fromAccountId)
    const bankAdhoc = findTransferMatrixRow(matrix, 'BANK', reference);
    return bankAdhoc ? [...bankAdhoc.toAccountTypes] : null;
  }
  const row = findTransferMatrixRow(matrix, fromAccountType, reference);
  return row ? [...row.toAccountTypes] : null;
}
