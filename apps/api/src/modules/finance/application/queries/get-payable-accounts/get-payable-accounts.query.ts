export type PayableAccountReference = 'ADHOC' | 'ADVANCE_EV';

/** When set, filters payable accounts for a product purpose (e.g. earning interest). */
export type PayableAccountPurpose = 'EARNING_INTEREST' | 'DONATION' | 'INVESTMENT_FUNDING';

export class GetPayableAccountsQuery {
  constructor(
    public readonly reference?: PayableAccountReference,
    public readonly fromAccountId?: string,
    public readonly purpose?: PayableAccountPurpose,
  ) {}
}
