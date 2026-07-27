import { PostgresAdvisoryLockingAdapter } from './postgres-advisory-locking.adapter';

describe('PostgresAdvisoryLockingAdapter', () => {
  it('passes the lock-owning transaction client to the callback', async () => {
    const tx = {
      $executeRaw: jest.fn().mockResolvedValue(undefined),
    };
    const prisma = {
      client: {
        $transaction: jest.fn(async (callback: any) => callback(tx)),
      },
    };
    const adapter = new PostgresAdvisoryLockingAdapter(prisma as any);
    const callback = jest.fn().mockResolvedValue('done');

    const result = await adapter.withLock('token:1', callback);

    expect(result).toBe('done');
    expect(callback).toHaveBeenCalledWith(tx);
  });

  it('deduplicates and sorts keys before acquiring locks', async () => {
    const tx = {
      $executeRaw: jest.fn().mockResolvedValue(undefined),
    };
    const prisma = {
      client: {
        $transaction: jest.fn(async (callback: any) => callback(tx)),
      },
    };
    const adapter = new PostgresAdvisoryLockingAdapter(prisma as any);

    await adapter.withLocks(['b', 'a', 'b'], jest.fn().mockResolvedValue('done'));

    expect(tx.$executeRaw).toHaveBeenCalledTimes(2);
  });
});
