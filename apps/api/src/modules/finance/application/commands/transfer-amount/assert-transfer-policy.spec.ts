import { AccountOwnerType } from '../../../domain/enums/account-owner-type.enum';
import { AccountStatus } from '../../../domain/enums/account-status.enum';
import { AccountType } from '../../../domain/enums/account-type.enum';
import { assertTransferPolicy } from './assert-transfer-policy';

function accountStub(props: {
  id: string;
  type: AccountType;
  status?: AccountStatus;
  ownerType?: AccountOwnerType;
  custodianUserIds?: string[];
}): any {
  return {
    id: props.id,
    type: props.type,
    status: props.status ?? AccountStatus.ACTIVE,
    ownerType: props.ownerType ?? AccountOwnerType.INDIVIDUAL,
    custodianUserIds: props.custodianUserIds ?? [],
  };
}

describe('assertTransferPolicy', () => {
  it('allows wallet to bank with ADHOC', () => {
    expect(() =>
      assertTransferPolicy(
        accountStub({ id: 'w1', type: AccountType.WALLET }),
        accountStub({ id: 'b1', type: AccountType.BANK }),
        'ADHOC',
      ),
    ).not.toThrow();
  });

  it('rejects wallet to wallet', () => {
    expect(() =>
      assertTransferPolicy(
        accountStub({ id: 'w1', type: AccountType.WALLET }),
        accountStub({ id: 'w2', type: AccountType.WALLET }),
        'ADHOC',
      ),
    ).toThrow('Wallet surplus can only be transferred to a bank account');
  });

  it('rejects wallet ADVANCE_EV', () => {
    expect(() =>
      assertTransferPolicy(
        accountStub({ id: 'w1', type: AccountType.WALLET }),
        accountStub({ id: 'b1', type: AccountType.BANK }),
        'ADVANCE_EV',
      ),
    ).toThrow('Wallet transfers only support General (ADHOC) reference');
  });

  it('allows bank to wallet ADVANCE_EV', () => {
    expect(() =>
      assertTransferPolicy(
        accountStub({ id: 'b1', type: AccountType.BANK }),
        accountStub({ id: 'w1', type: AccountType.WALLET }),
        'ADVANCE_EV',
      ),
    ).not.toThrow();
  });

  it('rejects investment as from or to', () => {
    expect(() =>
      assertTransferPolicy(
        accountStub({ id: 'i1', type: AccountType.INVESTMENT }),
        accountStub({ id: 'b1', type: AccountType.BANK }),
        'ADHOC',
      ),
    ).toThrow('Investment accounts cannot be part of a transfer');
  });

  it('rejects same account', () => {
    expect(() =>
      assertTransferPolicy(
        accountStub({ id: 'b1', type: AccountType.BANK }),
        accountStub({ id: 'b1', type: AccountType.BANK }),
        'ADHOC',
      ),
    ).toThrow('Cannot transfer to the same account');
  });

  it('allows org bank transfer when actor is custodian', () => {
    expect(() =>
      assertTransferPolicy(
        accountStub({
          id: 'b1',
          type: AccountType.BANK,
          ownerType: AccountOwnerType.ORG,
          custodianUserIds: ['user-1'],
        }),
        accountStub({ id: 'w1', type: AccountType.WALLET }),
        'ADVANCE_EV',
        'user-1',
      ),
    ).not.toThrow();
  });

  it('rejects org bank transfer when actor is not custodian', () => {
    expect(() =>
      assertTransferPolicy(
        accountStub({
          id: 'b1',
          type: AccountType.BANK,
          ownerType: AccountOwnerType.ORG,
          custodianUserIds: ['user-1'],
        }),
        accountStub({ id: 'w1', type: AccountType.WALLET }),
        'ADVANCE_EV',
        'user-2',
      ),
    ).toThrow('Only custodians can transfer from an organization account');
  });
});
