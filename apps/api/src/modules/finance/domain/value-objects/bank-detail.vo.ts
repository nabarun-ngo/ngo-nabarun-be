export class BankDetail {
  constructor(
    public bankAccountHolderName?: string,
    public bankName?: string,
    public bankBranch?: string,
    public bankAccountNumber?: string,
    public bankAccountType?: string,
    public IFSCNumber?: string,
    public maturityDate?: string,
    public maturityAmount?: number,
    public investmentAmount?: number,
    public sourceAccountId?: string,
    public dematId?: string,
    public interestRate?: number,
    public interestPayingTerm?: string,
  ) { }
}
