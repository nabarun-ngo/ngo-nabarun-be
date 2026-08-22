import { CommandBus, EventBus } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { AccountStatus } from '../../../domain/enums/account-status.enum';
import { AccountType } from '../../../domain/enums/account-type.enum';
import { EarningCategory, EarningStatus } from '../../../domain/enums/earning.enum';
import { TransactionRefType, TransactionType } from '../../../domain/enums/transaction.enum';
import { CreateEarningCommand } from './create-earning.command';
import { CreateEarningHandler } from './create-earning.handler';

describe('CreateEarningHandler', () => {
  const baseRequest = {
    userId: 'USER1',
    category: EarningCategory.INTEREST,
    amount: 500,
    currency: 'INR',
    source: 'Quarterly interest',
    description: 'Savings interest',
    accountId: 'ACC1',
  };

  const createHandler = (account: object | null = {
    id: 'ACC1',
    status: AccountStatus.ACTIVE,
    type: AccountType.BANK,
  }) => {
    const earningRepository = {
      create: jest.fn(async earning => earning),
    };
    const accountRepository = {
      findById: jest.fn().mockResolvedValue(account),
    };
    const commandBus = {
      execute: jest.fn().mockResolvedValue('TXR123'),
    } as unknown as CommandBus;
    const eventBus = {
      publishAll: jest.fn(),
    } as unknown as EventBus;
    const handler = new CreateEarningHandler(
      earningRepository as any,
      accountRepository as any,
      commandBus,
      eventBus,
    );
    return { handler, earningRepository, accountRepository, commandBus };
  };

  it.each([AccountType.BANK, AccountType.INVESTMENT])(
    'creates a received interest earning and credits an active %s account',
    async accountType => {
      const { handler, earningRepository, commandBus } = createHandler({
        id: 'ACC1',
        status: AccountStatus.ACTIVE,
        type: accountType,
      });

      const earning = await handler.execute(new CreateEarningCommand(baseRequest));

      expect(earning.status).toBe(EarningStatus.RECEIVED);
      expect(earning.accountId).toBe('ACC1');
      expect(earning.referenceId).toBe('ACC1');
      expect(earning.referenceType).toBe('ACCOUNT');
      expect(earning.transactionId).toBe('TXR123');
      expect(earning.earningDate).toBeInstanceOf(Date);
      expect(commandBus.execute).toHaveBeenCalledWith(expect.objectContaining({
        params: expect.objectContaining({
          accountId: 'ACC1',
          txnAmount: 500,
          txnRefId: earning.id,
          txnRefType: TransactionRefType.EARNING,
          txnType: TransactionType.IN,
        }),
      }));
      expect(earningRepository.create).toHaveBeenCalledWith(earning);
    },
  );

  it('requires an account for interest earnings', async () => {
    const { handler, commandBus } = createHandler();

    await expect(handler.execute(new CreateEarningCommand({
      ...baseRequest,
      accountId: undefined,
    }))).rejects.toBeInstanceOf(BusinessException);
    expect(commandBus.execute).not.toHaveBeenCalled();
  });

  it('rejects inactive and unsupported receive accounts', async () => {
    const inactive = createHandler({
      id: 'ACC1',
      status: AccountStatus.CLOSED,
      type: AccountType.BANK,
    });
    await expect(
      inactive.handler.execute(new CreateEarningCommand(baseRequest)),
    ).rejects.toBeInstanceOf(BusinessException);

    const wallet = createHandler({
      id: 'ACC1',
      status: AccountStatus.ACTIVE,
      type: AccountType.WALLET,
    });
    await expect(
      wallet.handler.execute(new CreateEarningCommand(baseRequest)),
    ).rejects.toBeInstanceOf(BusinessException);
  });

  it('keeps non-interest earnings pending without creating a transaction', async () => {
    const { handler, accountRepository, commandBus } = createHandler();

    const earning = await handler.execute(new CreateEarningCommand({
      ...baseRequest,
      category: EarningCategory.GRANT,
      accountId: undefined,
    }));

    expect(earning.status).toBe(EarningStatus.PENDING);
    expect(earning.accountId).toBeUndefined();
    expect(earning.transactionId).toBeUndefined();
    expect(accountRepository.findById).not.toHaveBeenCalled();
    expect(commandBus.execute).not.toHaveBeenCalled();
  });
});
