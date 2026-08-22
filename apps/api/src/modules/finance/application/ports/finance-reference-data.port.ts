export interface KeyValueOption {
  key: string;
  value: string;
  description?: string;
}

/** Shared outstanding / closed / excluded status buckets for list chips. */
export interface StatusGroupsConfig {
  outstanding: string[];
  closed: string[];
  excluded: string[];
}

/** @deprecated Use StatusGroupsConfig */
export type DonationStatusGroupsConfig = StatusGroupsConfig;

export interface TransferMatrixRow {
  fromAccountType: string;
  reference: string;
  toAccountTypes: string[];
}

export interface DonorStatusRulesConfig {
  statusesRequiringEndDate: string[];
}

export type AccountReferenceData = {
  accountStatuses: KeyValueOption[];
  accountTypes: KeyValueOption[];
  ownerTypes: KeyValueOption[];
  bankAccountTypes: KeyValueOption[];
  investmentTypes: KeyValueOption[];
  interestPayingTerms: KeyValueOption[];
  transferReferenceTypes: KeyValueOption[];
  transferMatrix: TransferMatrixRow[];
  transactionTypes: KeyValueOption[];
  transactionStatuses: KeyValueOption[];
  transactionRefTypes: KeyValueOption[];
  accountStatusGroups: StatusGroupsConfig;
};

export type ExpenseReferenceData = {
  expenseStatuses: KeyValueOption[];
  expenseRefTypes: KeyValueOption[];
  expenseStatusGroups: StatusGroupsConfig;
};

export type EarningReferenceData = {
  earningStatuses: KeyValueOption[];
  earningCategories: KeyValueOption[];
  earningStatusGroups: StatusGroupsConfig;
};

export type DonorReferenceData = {
  donorStatuses: KeyValueOption[];
  memberEditableDonorStatuses: KeyValueOption[];
  statusesRequiringEndDate: string[];
};

export type DonationReferenceData = {
  donationStatuses: KeyValueOption[];
  donationTypes: KeyValueOption[];
  paymentMethods: KeyValueOption[];
  upiOptions: KeyValueOption[];
  donationStatusGroups: StatusGroupsConfig;
};

export const IFinanceReferenceDataPort = Symbol('IFinanceReferenceDataPort');

export interface IFinanceReferenceDataPort {
  getDonationReferenceData(): Promise<DonationReferenceData>;
  getDonorReferenceData(): Promise<DonorReferenceData>;
  getExpenseReferenceData(): Promise<ExpenseReferenceData>;
  getAccountReferenceData(): Promise<AccountReferenceData>;
  getEarningReferenceData(): Promise<EarningReferenceData>;
}
