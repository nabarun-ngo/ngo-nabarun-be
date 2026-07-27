import { Injectable, Logger } from '@nestjs/common';
import {
  BasePrismaService,
  ILockingPort,
  LockTransaction,
} from '@nabarun-ngo/nestjs-shared-persistence';

/** Minimal tx client shape used for Postgres advisory-lock transactions. */
type AdvisoryLockTransaction = {
  $executeRaw: (
    query: TemplateStringsArray,
    ...values: unknown[]
  ) => Promise<unknown>;
};

/**
 * Postgres advisory-lock implementation of {@link ILockingPort}.
 * Register in the host PersistenceModule when using PostgreSQL.
 */
@Injectable()
export class PostgresAdvisoryLockingAdapter implements ILockingPort {
  private readonly logger = new Logger(PostgresAdvisoryLockingAdapter.name);

  constructor(private readonly prisma: BasePrismaService) {}

  async withLock<T>(
    key: string,
    fn: (tx: LockTransaction) => Promise<T>,
  ): Promise<T> {
    return this.withLocks([key], fn);
  }

  async withLocks<T>(
    keys: string[],
    fn: (tx?: LockTransaction) => Promise<T>,
  ): Promise<T> {
    if (keys.length === 0) return await fn();

    const sortedKeys = [...new Set(keys)].sort();

    this.logger.debug(`Acquiring postgres locks for: ${sortedKeys.join(', ')}`);

    return await this.prisma.client.$transaction(
      async (tx) => {
        const txClient = tx as AdvisoryLockTransaction;
        for (const key of sortedKeys) {
          await txClient.$executeRaw`SELECT pg_advisory_xact_lock(hashtextextended(${key}, 0))`;
        }
        this.logger.debug(
          `Acquired all postgres locks: ${sortedKeys.join(', ')}`,
        );
        try {
          return await fn(txClient);
        } catch (error) {
          this.logger.error(
            `Error while holding locks for [${sortedKeys.join(', ')}]`,
            error,
          );
          throw error;
        }
      },
      {
        timeout: 60000,
      },
    );
  }
}
