import { AccountType } from '../enums/account-type.enum';
import {
  assertBankNameBranchMatchesLookup,
  assertBalanceNearEstimatedMaturity,
  assertCreateAccountDetails,
  assertIfscFormat,
  assertImmutableInvestmentFundingFields,
  assertRequiredBankDetailAtCreate,
  assertUpdateAccountDetails,
} from './account-detail.validation';

const completeBank = {
  bankAccountNumber: '123',
  bankAccountHolderName: 'Jane',
  bankName: 'Demo Bank',
  bankAccountType: 'Savings',
  bankBranch: 'Kolkata',
  IFSCNumber: 'STDB0001234',
};

describe('account-detail.validation IFSC', () => {
  it('requires IFSC format for bank accounts at create', () => {
    expect(() =>
      assertRequiredBankDetailAtCreate(AccountType.BANK, {
        ...completeBank,
        IFSCNumber: 'INVALID',
      }),
    ).toThrow('Invalid IFSC format');
  });

  it('assertIfscFormat rejects malformed codes', () => {
    expect(() => assertIfscFormat('1234')).toThrow('Invalid IFSC format');
    expect(() => assertIfscFormat('STDB0001234')).not.toThrow();
  });

  it('assertBankNameBranchMatchesLookup rejects mismatched bank name', () => {
    expect(() =>
      assertBankNameBranchMatchesLookup(
        {
          IFSCNumber: 'STDB0001234',
          bankName: 'Wrong Bank',
          bankBranch: 'Salt Lake',
        },
        { bankName: 'State Demo Bank', branch: 'Salt Lake' },
      ),
    ).toThrow('Bank name does not match IFSC lookup');
  });

  it('assertBankNameBranchMatchesLookup rejects mismatched branch', () => {
    expect(() =>
      assertBankNameBranchMatchesLookup(
        {
          IFSCNumber: 'STDB0001234',
          bankName: 'State Demo Bank',
          bankBranch: 'Park Street',
        },
        { bankName: 'State Demo Bank', branch: 'Salt Lake' },
      ),
    ).toThrow('Bank branch does not match IFSC lookup');
  });
});

describe('assertCreateAccountDetails', () => {
  it('allows WALLET create with no bank and no UPI', () => {
    expect(() =>
      assertCreateAccountDetails(AccountType.WALLET, undefined),
    ).not.toThrow();
  });

  it('rejects partial WALLET bank detail on create (all-or-nothing)', () => {
    expect(() =>
      assertCreateAccountDetails(AccountType.WALLET, { bankAccountNumber: '123' }),
    ).toThrow('Bank account holder name is required');
  });

  it('allows WALLET create with complete bank detail', () => {
    expect(() =>
      assertCreateAccountDetails(AccountType.WALLET, completeBank),
    ).not.toThrow();
  });

  it('allows WALLET create with optional UPI', () => {
    expect(() =>
      assertCreateAccountDetails(AccountType.WALLET, undefined, { upiDetailsCount: 1 }),
    ).not.toThrow();
  });

  it('allows BANK create with complete bank detail and optional UPI', () => {
    expect(() =>
      assertCreateAccountDetails(AccountType.BANK, completeBank, { upiDetailsCount: 1 }),
    ).not.toThrow();
  });

  it('requires bank detail for BANK create', () => {
    expect(() =>
      assertCreateAccountDetails(AccountType.BANK, undefined),
    ).toThrow('Bank detail is required for this account type');
  });

  it('rejects UPI on INVESTMENT create', () => {
    expect(() =>
      assertCreateAccountDetails(
        AccountType.INVESTMENT,
        {
          bankAccountNumber: 'FOLIO-1',
          bankName: 'Provider',
          bankAccountType: 'FD',
        },
        { upiDetailsCount: 1, description: 'FD note' },
      ),
    ).toThrow('UPI details are only allowed on bank and wallet accounts');
  });

  it('requires investment description on create', () => {
    expect(() =>
      assertCreateAccountDetails(
        AccountType.INVESTMENT,
        {
          bankAccountNumber: 'FOLIO-1',
          bankName: 'Provider',
          bankAccountType: 'FD',
        },
      ),
    ).toThrow('Description is required for investment accounts');
  });

  it('requires investment amount and source bank for INVESTMENT create', () => {
    expect(() =>
      assertCreateAccountDetails(
        AccountType.INVESTMENT,
        {
          bankAccountNumber: 'FOLIO-1',
          bankName: 'Provider',
          bankAccountType: 'FD',
        },
        { description: 'FD note' },
      ),
    ).toThrow('Investment amount must be greater than zero');
  });

  it('requires source bank after investment amount for INVESTMENT create', () => {
    expect(() =>
      assertCreateAccountDetails(
        AccountType.INVESTMENT,
        {
          bankAccountNumber: 'FOLIO-1',
          bankName: 'Provider',
          bankAccountType: 'FD',
          investmentAmount: 100000,
        },
        { description: 'FD note' },
      ),
    ).toThrow('Source bank account is required for investment accounts');
  });

  it('rejects zero estimated maturity amount when provided on INVESTMENT create', () => {
    expect(() =>
      assertCreateAccountDetails(
        AccountType.INVESTMENT,
        {
          bankAccountNumber: 'FOLIO-1',
          bankName: 'Provider',
          bankAccountType: 'FD',
          investmentAmount: 100000,
          sourceAccountId: 'bank-1',
          maturityAmount: 0,
        },
        { description: 'FD note' },
      ),
    ).toThrow('Estimated maturity amount must be greater than zero when provided');
  });

  it('allows INVESTMENT create with funding and optional maturity details', () => {
    expect(() =>
      assertCreateAccountDetails(
        AccountType.INVESTMENT,
        {
          bankAccountNumber: 'FOLIO-1',
          bankName: 'Provider',
          bankAccountType: 'FD',
          investmentAmount: 100000,
          sourceAccountId: 'bank-1',
          maturityDate: '2027-08-14',
          maturityAmount: 150000,
        },
        { description: 'FD note' },
      ),
    ).not.toThrow();
  });

  it('allows optional interest details on INVESTMENT create', () => {
    expect(() =>
      assertCreateAccountDetails(
        AccountType.INVESTMENT,
        {
          bankAccountNumber: 'FOLIO-1',
          bankName: 'Provider',
          bankAccountType: 'FD',
          investmentAmount: 100000,
          sourceAccountId: 'bank-1',
          interestRate: 7.5,
          interestPayingTerm: 'QUARTERLY',
        },
        { description: 'FD note' },
      ),
    ).not.toThrow();
  });

  it('rejects demat on INVESTMENT create', () => {
    expect(() =>
      assertCreateAccountDetails(
        AccountType.INVESTMENT,
        {
          bankAccountNumber: 'FOLIO-1',
          bankName: 'Provider',
          bankAccountType: 'FD',
          investmentAmount: 100000,
          sourceAccountId: 'bank-1',
          dematId: 'DEMAT-1',
        },
        { description: 'FD note' },
      ),
    ).toThrow('Demat details can only be added when updating an account');
  });
});

describe('assertUpdateAccountDetails', () => {
  it('allows BANK update with complete bank detail and optional UPI', () => {
    expect(() =>
      assertUpdateAccountDetails(AccountType.BANK, completeBank, { upiDetailsCount: 1 }),
    ).not.toThrow();
  });

  it('rejects incomplete BANK bank detail on update', () => {
    expect(() =>
      assertUpdateAccountDetails(AccountType.BANK, { bankAccountNumber: '123' }),
    ).toThrow('Bank account holder name is required');
  });

  it('allows WALLET update with no bank detail', () => {
    expect(() =>
      assertUpdateAccountDetails(AccountType.WALLET, undefined, { upiDetailsCount: 1 }),
    ).not.toThrow();
  });

  it('rejects partial WALLET bank detail (all-or-nothing)', () => {
    expect(() =>
      assertUpdateAccountDetails(AccountType.WALLET, { bankAccountNumber: '123' }),
    ).toThrow('Bank account holder name is required');
  });

  it('allows WALLET update with complete bank detail', () => {
    expect(() =>
      assertUpdateAccountDetails(AccountType.WALLET, completeBank),
    ).not.toThrow();
  });

  it('rejects UPI on INVESTMENT update', () => {
    expect(() =>
      assertUpdateAccountDetails(
        AccountType.INVESTMENT,
        {
          bankAccountNumber: 'FOLIO-1',
          bankName: 'Provider',
          bankAccountType: 'FD',
        },
        { upiDetailsCount: 1, description: 'FD note' },
      ),
    ).toThrow('UPI details are only allowed on bank and wallet accounts');
  });

  it('requires investment description when provided empty', () => {
    expect(() =>
      assertUpdateAccountDetails(
        AccountType.INVESTMENT,
        {
          bankAccountNumber: 'FOLIO-1',
          bankName: 'Provider',
          bankAccountType: 'FD',
        },
        { description: '   ' },
      ),
    ).toThrow('Description is required for investment accounts');
  });

  it('allows status-only BANK update without bankDetail in request', () => {
    expect(() =>
      assertUpdateAccountDetails(AccountType.BANK, undefined),
    ).not.toThrow();
  });
});

describe('assertImmutableInvestmentFundingFields', () => {
  const existing = {
    bankAccountNumber: 'FOLIO-1',
    bankName: 'Provider',
    bankAccountType: 'FD',
    investmentAmount: 100000,
    sourceAccountId: 'bank-1',
  };

  it('rejects investment amount changes', () => {
    expect(() =>
      assertImmutableInvestmentFundingFields(existing, {
        ...existing,
        investmentAmount: 120000,
      }),
    ).toThrow('Investment amount cannot be changed after create');
  });

  it('rejects source bank changes', () => {
    expect(() =>
      assertImmutableInvestmentFundingFields(existing, {
        ...existing,
        sourceAccountId: 'bank-2',
      }),
    ).toThrow('Source bank account cannot be changed after create');
  });

  it('allows maturity edits while funding stays the same', () => {
    expect(() =>
      assertImmutableInvestmentFundingFields(existing, {
        ...existing,
        maturityDate: '2028-01-01',
        maturityAmount: 150000,
      }),
    ).not.toThrow();
  });
});

describe('assertBalanceNearEstimatedMaturity', () => {
  it('allows balance within max(100, 1%) of estimated', () => {
    expect(() => assertBalanceNearEstimatedMaturity(150050, 150000)).not.toThrow();
    expect(() => assertBalanceNearEstimatedMaturity(10100, 10000)).not.toThrow();
  });

  it('rejects balance outside tolerance', () => {
    expect(() => assertBalanceNearEstimatedMaturity(148000, 150000)).toThrow(
      /not within/,
    );
  });

  it('skips proximity when estimated is not positive', () => {
    expect(() => assertBalanceNearEstimatedMaturity(500, 0)).not.toThrow();
  });
});
