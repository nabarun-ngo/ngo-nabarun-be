import { ACCOUNT_TYPE_CONFIG } from '../config/account-type.config';
import { AccountType } from '../enums/account-type.enum';
import { isValidIfscFormat, normalizeIfsc } from './ifsc.validation';

export interface BankDetailInput {
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

function hasOptionalInvestmentMetadata(bank: BankDetailInput): boolean {
  return !!(
    bank.dematId?.trim()
    || bank.interestRate != null
    || bank.interestPayingTerm?.trim()
    || bank.investmentAmount != null
    || bank.sourceAccountId?.trim()
  );
}

/** Demat remains update-only. */
function hasUpdateOnlyInvestmentMetadata(bank: BankDetailInput): boolean {
  return !!bank.dematId?.trim();
}

function hasAnyBankField(bank: BankDetailInput | undefined): boolean {
  if (!bank) {
    return false;
  }
  return !!(
    bank.bankAccountNumber?.trim()
    || bank.bankAccountHolderName?.trim()
    || bank.bankName?.trim()
    || bank.bankBranch?.trim()
    || bank.bankAccountType?.trim()
    || bank.IFSCNumber?.trim()
    || bank.maturityDate?.trim()
    || bank.maturityAmount != null
    || hasOptionalInvestmentMetadata(bank)
  );
}

function isUpiAllowed(type: AccountType): boolean {
  const config = ACCOUNT_TYPE_CONFIG[type];
  return config.requiredDetails.includes('upiDetails')
    || config.optionalDetails.includes('upiDetails');
}

/**
 * Create matrix:
 * - BANK: bankDetail required (complete); UPI optional
 * - WALLET: bank optional (all-or-nothing when started); UPI optional
 * - INVESTMENT: folio/provider/type, investmentAmount, sourceAccountId, description required;
 *   maturity date/amount optional; interest optional; demat update-only; UPI not allowed
 */
export function assertCreateAccountDetails(
  type: AccountType,
  bankDetail: BankDetailInput | undefined,
  options: { upiDetailsCount?: number; description?: string } = {},
): void {
  if ((options.upiDetailsCount ?? 0) > 0 && !isUpiAllowed(type)) {
    throw new Error('UPI details are only allowed on bank and wallet accounts');
  }

  if (type === AccountType.INVESTMENT && !options.description?.trim()) {
    throw new Error('Description is required for investment accounts');
  }

  const config = ACCOUNT_TYPE_CONFIG[type];
  if (config.requiredDetails.includes('bankDetail')) {
    if (!bankDetail) {
      throw new Error('Bank detail is required for this account type');
    }
    assertRequiredBankDetailAtCreate(type, bankDetail);
    return;
  }

  if (type === AccountType.WALLET && hasAnyBankField(bankDetail)) {
    assertRequiredBankDetailAtCreate(AccountType.WALLET, bankDetail!);
  }
}

export function assertRequiredBankDetailAtCreate(type: AccountType, bank: BankDetailInput): void {
  if (hasUpdateOnlyInvestmentMetadata(bank)) {
    throw new Error('Demat details can only be added when updating an account');
  }
  if (type !== AccountType.INVESTMENT && (bank.maturityDate?.trim() || bank.maturityAmount != null)) {
    throw new Error('Maturity details are only allowed for investment accounts');
  }
  if (type !== AccountType.INVESTMENT && (bank.interestRate != null || bank.interestPayingTerm?.trim())) {
    throw new Error('Interest details are only allowed for investment accounts');
  }
  if (type !== AccountType.INVESTMENT && (bank.investmentAmount != null || bank.sourceAccountId?.trim())) {
    throw new Error('Investment funding details are only allowed for investment accounts');
  }

  assertRequiredBankDetailAtUpdate(type, bank);

  if (type === AccountType.INVESTMENT) {
    if (bank.investmentAmount == null || !Number.isFinite(bank.investmentAmount) || bank.investmentAmount <= 0) {
      throw new Error('Investment amount must be greater than zero');
    }
    requireField(bank.sourceAccountId, 'Source bank account is required for investment accounts');
    if (bank.maturityAmount != null && (!Number.isFinite(bank.maturityAmount) || bank.maturityAmount <= 0)) {
      throw new Error('Estimated maturity amount must be greater than zero when provided');
    }
  }
}

export function assertRequiredBankDetailAtUpdate(type: AccountType, bank: BankDetailInput): void {
  if (type === AccountType.BANK || type === AccountType.WALLET) {
    requireField(bank.bankAccountNumber, 'Bank account number is required');
    requireField(bank.bankAccountHolderName, 'Bank account holder name is required');
    requireField(bank.bankName, 'Bank name is required');
    requireField(bank.bankAccountType, 'Bank account type is required');
    requireField(bank.bankBranch, 'Bank branch is required');
    requireField(bank.IFSCNumber, 'IFSC is required for bank accounts');
    assertIfscFormat(bank.IFSCNumber);
    return;
  }

  if (type === AccountType.INVESTMENT) {
    requireField(bank.bankAccountNumber, 'Account or folio number is required');
    requireField(bank.bankName, 'Provider name is required');
    requireField(bank.bankAccountType, 'Investment type is required');
    if (bank.maturityAmount != null && (!Number.isFinite(bank.maturityAmount) || bank.maturityAmount <= 0)) {
      throw new Error('Estimated maturity amount must be greater than zero when provided');
    }
  }
}

/**
 * Immutable funding fields must not change after create.
 */
export function assertImmutableInvestmentFundingFields(
  existing: BankDetailInput | undefined,
  next: BankDetailInput,
): void {
  const existingAmount = existing?.investmentAmount;
  const nextAmount = next.investmentAmount;
  if (
    existingAmount != null
    && nextAmount != null
    && Number(existingAmount) !== Number(nextAmount)
  ) {
    throw new Error('Investment amount cannot be changed after create');
  }
  if (nextAmount != null && existingAmount == null) {
    throw new Error('Investment amount cannot be changed after create');
  }
  if (
    existing?.sourceAccountId?.trim()
    && next.sourceAccountId?.trim()
    && existing.sourceAccountId.trim() !== next.sourceAccountId.trim()
  ) {
    throw new Error('Source bank account cannot be changed after create');
  }
  if (next.sourceAccountId?.trim() && !existing?.sourceAccountId?.trim()) {
    throw new Error('Source bank account cannot be changed after create');
  }
}

/**
 * Close proximity: abs(balance - estimated) <= max(100, 1% of estimated).
 */
export function assertBalanceNearEstimatedMaturity(
  balance: number,
  estimatedMaturityAmount: number,
): void {
  if (!Number.isFinite(estimatedMaturityAmount) || estimatedMaturityAmount <= 0) {
    return;
  }
  const tolerance = Math.max(100, estimatedMaturityAmount * 0.01);
  const delta = Math.abs(balance - estimatedMaturityAmount);
  if (delta > tolerance) {
    throw new Error(
      `Investment balance ${balance} is not within ${tolerance} of estimated maturity amount ${estimatedMaturityAmount}`,
    );
  }
}

/**
 * Edit/update matrix:
 * - BANK: bankDetail when present must be complete; UPI allowed
 * - WALLET: bank optional (all-or-nothing); UPI allowed
 * - INVESTMENT: bankDetail when present must be complete; UPI not allowed
 */
export function assertUpdateAccountDetails(
  type: AccountType,
  bankDetail: BankDetailInput | undefined,
  options: {
    upiDetailsCount?: number;
    description?: string;
    existingBankDetail?: BankDetailInput;
  } = {},
): void {
  if ((options.upiDetailsCount ?? 0) > 0 && !isUpiAllowed(type)) {
    throw new Error('UPI details are only allowed on bank and wallet accounts');
  }

  if (type === AccountType.INVESTMENT && options.description !== undefined && !options.description.trim()) {
    throw new Error('Description is required for investment accounts');
  }

  const config = ACCOUNT_TYPE_CONFIG[type];
  if (config.requiredDetails.includes('bankDetail')) {
    if (bankDetail) {
      assertRequiredBankDetailAtUpdate(type, bankDetail);
      if (type === AccountType.INVESTMENT) {
        assertImmutableInvestmentFundingFields(options.existingBankDetail, bankDetail);
      }
    }
    return;
  }

  if (type === AccountType.WALLET && hasAnyBankField(bankDetail)) {
    assertRequiredBankDetailAtUpdate(AccountType.WALLET, bankDetail!);
  }
}

function requireField(value: string | undefined, message: string): void {
  if (!value?.trim()) {
    throw new Error(message);
  }
}

export function assertIfscFormat(ifsc: string | undefined): void {
  if (!ifsc?.trim()) {
    return;
  }
  if (!isValidIfscFormat(ifsc)) {
    throw new Error('Invalid IFSC format');
  }
}

export function assertBankNameBranchMatchesLookup(
  bank: BankDetailInput,
  lookup: { bankName: string; branch: string },
): void {
  const normalizedIfsc = normalizeIfsc(bank.IFSCNumber ?? '');
  if (!normalizedIfsc) {
    return;
  }
  if (bank.bankName?.trim() !== lookup.bankName.trim()) {
    throw new Error('Bank name does not match IFSC lookup');
  }
  if (bank.bankBranch?.trim() !== lookup.branch.trim()) {
    throw new Error('Bank branch does not match IFSC lookup');
  }
}

export async function assertUpdateBankDetailIfsc(
  accountType: AccountType,
  bank: BankDetailInput | undefined,
  validateIfsc: (bank: BankDetailInput) => Promise<void>,
): Promise<void> {
  if (!bank?.IFSCNumber?.trim()) {
    return;
  }
  if (accountType !== AccountType.BANK && accountType !== AccountType.WALLET) {
    return;
  }
  assertIfscFormat(bank.IFSCNumber);
  await validateIfsc(bank);
}
