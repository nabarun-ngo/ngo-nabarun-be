import { AccountOwnerType } from '../../../domain/enums/account-owner-type.enum';
import { AccountStatus } from '../../../domain/enums/account-status.enum';
import { AccountType } from '../../../domain/enums/account-type.enum';
import { GetPayableAccountsHandler } from './get-payable-accounts.handler';
import { GetPayableAccountsQuery } from './get-payable-accounts.query';

describe('GetPayableAccountsHandler', () => {
  const repo = {
    findAll: jest.fn(),
    findById: jest.fn(),
  };

  const handler = new GetPayableAccountsHandler(repo as any);

  beforeEach(() => {
    jest.clearAllMocks();
    repo.findAll.mockResolvedValue([]);
  });

  it('uses BANK + ORG filters when reference is omitted', async () => {
    await handler.execute(new GetPayableAccountsQuery());

    expect(repo.findAll).toHaveBeenCalledWith({
      type: [AccountType.BANK],
      ownerType: [AccountOwnerType.ORG],
      status: [AccountStatus.ACTIVE],
      includeBalance: false,
    });
  });

  it('returns active bank and investment accounts for interest earnings', async () => {
    await handler.execute(
      new GetPayableAccountsQuery(undefined, undefined, 'EARNING_INTEREST'),
    );

    expect(repo.findAll).toHaveBeenCalledWith({
      type: [AccountType.BANK, AccountType.INVESTMENT],
      status: [AccountStatus.ACTIVE],
      includeBalance: false,
    });
  });

  it('uses BANK only when from WALLET + ADHOC', async () => {
    repo.findById.mockResolvedValue({ id: 'w1', type: AccountType.WALLET });

    await handler.execute(new GetPayableAccountsQuery('ADHOC', 'w1'));

    expect(repo.findAll).toHaveBeenCalledWith({
      type: [AccountType.BANK],
      ownerType: [],
      status: [AccountStatus.ACTIVE],
      includeBalance: false,
    });
  });

  it('returns empty when from WALLET + ADVANCE_EV', async () => {
    repo.findById.mockResolvedValue({ id: 'w1', type: AccountType.WALLET });

    const result = await handler.execute(new GetPayableAccountsQuery('ADVANCE_EV', 'w1'));

    expect(result).toEqual([]);
    expect(repo.findAll).not.toHaveBeenCalled();
  });

  it('uses BANK + WALLET when from BANK + ADHOC', async () => {
    repo.findById.mockResolvedValue({ id: 'b1', type: AccountType.BANK });

    await handler.execute(new GetPayableAccountsQuery('ADHOC', 'b1'));

    expect(repo.findAll).toHaveBeenCalledWith({
      type: [AccountType.BANK, AccountType.WALLET],
      ownerType: [],
      status: [AccountStatus.ACTIVE],
      includeBalance: false,
    });
  });

  it('uses WALLET only when from BANK + ADVANCE_EV', async () => {
    repo.findById.mockResolvedValue({ id: 'b1', type: AccountType.BANK });

    await handler.execute(new GetPayableAccountsQuery('ADVANCE_EV', 'b1'));

    expect(repo.findAll).toHaveBeenCalledWith({
      type: [AccountType.WALLET],
      ownerType: [],
      status: [AccountStatus.ACTIVE],
      includeBalance: false,
    });
  });

  it('returns empty when from INVESTMENT', async () => {
    repo.findById.mockResolvedValue({ id: 'i1', type: AccountType.INVESTMENT });

    const result = await handler.execute(new GetPayableAccountsQuery('ADHOC', 'i1'));

    expect(result).toEqual([]);
    expect(repo.findAll).not.toHaveBeenCalled();
  });

  it('excludes fromAccountId from results', async () => {
    repo.findById.mockResolvedValue({ id: 'b1', type: AccountType.BANK });
    repo.findAll.mockResolvedValue([
      { id: 'b1', type: AccountType.BANK, custodianUserIds: [] },
      { id: 'b2', type: AccountType.BANK, custodianUserIds: [] },
    ]);

    const result = await handler.execute(new GetPayableAccountsQuery('ADHOC', 'b1'));

    expect(result.map((a) => a.id)).toEqual(['b2']);
  });
});
