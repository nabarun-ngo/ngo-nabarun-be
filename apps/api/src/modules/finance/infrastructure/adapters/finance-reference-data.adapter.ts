import { Injectable, Logger } from '@nestjs/common';
import { JsonStoreFacade } from '@nabarun-ngo/nestjs-shared-json-store';
import {
  IFinanceReferenceDataPort,
  KeyValueOption,
  StatusGroupsConfig,
  TransferMatrixRow,
  AccountReferenceData,
  DonationReferenceData,
  DonorReferenceData,
  ExpenseReferenceData,
  EarningReferenceData,
} from '../../application/ports/finance-reference-data.port';
import { FinanceReferenceDataPayloadSchema } from '../../finance-reference-data.schema';
import { DEFAULT_TRANSFER_MATRIX } from '../../application/commands/transfer-amount/transfer-matrix';

@Injectable()
export class FinanceReferenceDataAdapter implements IFinanceReferenceDataPort {
  private static readonly NAMESPACE = 'finance-reference-data';
  private readonly logger = new Logger(FinanceReferenceDataAdapter.name);

  constructor(private readonly jsonStore: JsonStoreFacade) { }

  async getDonationReferenceData(): Promise<DonationReferenceData> {
    const [
      donationStatuses,
      donationTypes,
      paymentMethods,
      upiOptions,
      donationStatusGroups,
    ] = await Promise.all([
      this.loadItems('donation-statuses'),
      this.loadItems('donation-types'),
      this.loadItems('payment-methods'),
      this.loadItems('upi-options'),
      this.loadStatusGroups('donation-status-groups'),
    ]);
    return {
      donationStatuses,
      donationTypes,
      paymentMethods,
      upiOptions,
      donationStatusGroups,
    };
  }

  async getDonorReferenceData(): Promise<DonorReferenceData> {
    const [donorStatuses, memberEditableDonorStatuses, statusRules] = await Promise.all([
      this.loadItems('donor-statuses'),
      this.loadItems('member-editable-donor-statuses'),
      this.loadDonorStatusRules('donor-status-rules'),
    ]);
    return {
      donorStatuses,
      memberEditableDonorStatuses,
      statusesRequiringEndDate: statusRules.statusesRequiringEndDate,
    };
  }

  async getExpenseReferenceData(): Promise<ExpenseReferenceData> {
    const [expenseStatuses, expenseRefTypes, expenseStatusGroups] = await Promise.all([
      this.loadItems('expense-statuses'),
      this.loadItems('expense-categories'),
      this.loadStatusGroups('expense-status-groups'),
    ]);
    return { expenseStatuses, expenseRefTypes, expenseStatusGroups };
  }

  async getAccountReferenceData(): Promise<AccountReferenceData> {
    const [
      accountStatuses,
      accountTypes,
      ownerTypes,
      bankAccountTypes,
      investmentTypes,
      interestPayingTerms,
      transferReferenceTypes,
      transferMatrix,
      transactionTypes,
      transactionStatuses,
      transactionRefTypes,
      accountStatusGroups,
    ] = await Promise.all([
      this.loadItems('account-statuses'),
      this.loadItems('account-types'),
      this.loadItems('owner-types'),
      this.loadItems('bank-account-types'),
      this.loadItems('investment-types'),
      this.loadItems('interest-paying-terms'),
      this.loadItems('transfer-reference-types'),
      this.loadTransferMatrix('transfer-matrix'),
      this.loadItems('transaction-types'),
      this.loadItems('transaction-statuses'),
      this.loadItems('transaction-ref-types'),
      this.loadStatusGroups('account-status-groups'),
    ]);
    return {
      accountStatuses,
      accountTypes,
      ownerTypes,
      bankAccountTypes,
      investmentTypes,
      interestPayingTerms,
      transferReferenceTypes,
      transferMatrix,
      transactionTypes,
      transactionStatuses,
      transactionRefTypes,
      accountStatusGroups,
    };
  }

  async getEarningReferenceData(): Promise<EarningReferenceData> {
    const [earningStatuses, earningCategories, earningStatusGroups] = await Promise.all([
      this.loadItems('earning-statuses'),
      this.loadItems('earning-categories'),
      this.loadStatusGroups('earning-status-groups'),
    ]);
    return { earningStatuses, earningCategories, earningStatusGroups };
  }

  private async loadItems(key: string): Promise<KeyValueOption[]> {
    const payload = await this.jsonStore.get(key, FinanceReferenceDataAdapter.NAMESPACE);
    if (!payload) return [];
    const parsed = FinanceReferenceDataPayloadSchema.safeParse(payload);
    if (!parsed.success) {
      this.logger.warn(`Invalid finance-reference-data payload for ${key}`);
      return [];
    }
    if (!('items' in parsed.data)) {
      this.logger.warn(`Expected key-value finance-reference-data payload for ${key}`);
      return [];
    }
    return parsed.data.items;
  }

  private async loadStatusGroups(key: string): Promise<StatusGroupsConfig> {
    const payload = await this.jsonStore.get(key, FinanceReferenceDataAdapter.NAMESPACE);
    if (!payload || typeof payload !== 'object') {
      return { outstanding: [], closed: [], excluded: [] };
    }
    const record = payload as Record<string, unknown>;
    return {
      outstanding: Array.isArray(record['outstanding']) ? record['outstanding'] as string[] : [],
      closed: Array.isArray(record['closed']) ? record['closed'] as string[] : [],
      excluded: Array.isArray(record['excluded']) ? record['excluded'] as string[] : [],
    };
  }

  private async loadTransferMatrix(key: string): Promise<TransferMatrixRow[]> {
    const payload = await this.jsonStore.get(key, FinanceReferenceDataAdapter.NAMESPACE);
    if (!payload || typeof payload !== 'object') {
      return [...DEFAULT_TRANSFER_MATRIX];
    }
    const parsed = FinanceReferenceDataPayloadSchema.safeParse(payload);
    if (!parsed.success || !('rows' in parsed.data)) {
      this.logger.warn(`Invalid transfer-matrix finance-reference-data payload for ${key}`);
      return [...DEFAULT_TRANSFER_MATRIX];
    }
    return parsed.data.rows as TransferMatrixRow[];
  }

  private async loadDonorStatusRules(key: string): Promise<{ statusesRequiringEndDate: string[] }> {
    const payload = await this.jsonStore.get(key, FinanceReferenceDataAdapter.NAMESPACE);
    if (!payload || typeof payload !== 'object') {
      return { statusesRequiringEndDate: ['PAUSED', 'WAIVED'] };
    }
    const record = payload as Record<string, unknown>;
    const statuses = Array.isArray(record['statusesRequiringEndDate'])
      ? record['statusesRequiringEndDate'] as string[]
      : [];
    return {
      statusesRequiringEndDate: statuses.length ? statuses : ['PAUSED', 'WAIVED'],
    };
  }
}
