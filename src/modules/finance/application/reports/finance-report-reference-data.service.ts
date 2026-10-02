import { Inject, Injectable } from '@nestjs/common';
import { IFinanceReferenceDataPort, KeyValueOption } from '../ports/finance-reference-data.port';

function keyValueOptions(value: unknown): KeyValueOption[] {
  return Array.isArray(value) ? value as KeyValueOption[] : [];
}

export interface FinanceReportReferenceData {
  acc_type: KeyValueOption[];
  donationType: KeyValueOption[];
  donationStatus: KeyValueOption[];
  paymentMethod: KeyValueOption[];
  upiOption: KeyValueOption[];
  earn_categories: KeyValueOption[];
  earn_status: KeyValueOption[];
  exp_categories: KeyValueOption[];
  exp_status: KeyValueOption[];
}

@Injectable()
export class FinanceReportReferenceDataService {
  constructor(
    @Inject(IFinanceReferenceDataPort)
    private readonly referenceDataPort: IFinanceReferenceDataPort,
  ) {}

  async getReferenceData(): Promise<FinanceReportReferenceData> {
    const [donationRef, expenseRef, accountRef, earningRef] = await Promise.all([
      this.referenceDataPort.getDonationReferenceData(),
      this.referenceDataPort.getExpenseReferenceData(),
      this.referenceDataPort.getAccountReferenceData(),
      this.referenceDataPort.getEarningReferenceData(),
    ]);

    return {
      acc_type: keyValueOptions(accountRef.accountTypes),
      donationType: keyValueOptions(donationRef.donationTypes),
      donationStatus: keyValueOptions(donationRef.donationStatuses),
      paymentMethod: keyValueOptions(donationRef.paymentMethods),
      upiOption: keyValueOptions(donationRef.upiOptions),
      earn_categories: keyValueOptions(earningRef.earningCategories),
      earn_status: keyValueOptions(earningRef.earningStatuses),
      exp_categories: keyValueOptions(expenseRef.expenseRefTypes),
      exp_status: keyValueOptions(expenseRef.expenseStatuses),
    };
  }
}
