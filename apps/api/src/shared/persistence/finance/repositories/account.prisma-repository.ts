import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { BaseFilter, Page } from '@nabarun-ngo/nestjs-shared-core';
import { Prisma, PrismaClient } from '../../prisma/client';
import type {
  AccountWhereInput,
  AccountWhereUniqueInput,
  AccountUncheckedCreateInput,
  AccountUncheckedUpdateInput,
  AccountOrderByWithRelationInput,
} from '../../prisma/models/Account';
import { Account } from '../../../../modules/finance/domain/aggregates/account/account.aggregate';
import { AccountFilter, IAccountRepository } from '../../../../modules/finance/domain/repositories/account.repository';
import { AccountPrismaMapper } from '../mapper/account-prisma.mapper';
import { TransactionPrismaMapper } from '../mapper/transaction-prisma.mapper';

export type OnlyAccount = Prisma.AccountGetPayload<{
  include: {
    accountHolder: true;
  }
}>;

export type AccountWithRelations = Prisma.AccountGetPayload<{
  include: {
    accountHolder: true;
    transactions: true;
    bankInvestDetail: true;
    upiDetailRows: true;
  }
}>;

const ACCOUNT_RELATIONS = {
  accountHolder: true,
  bankInvestDetail: true,
  upiDetailRows: true,
} as const;

@Injectable()
export class AccountPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'account',
    Account,
    string,
    AccountFilter,
    AccountWithRelations,
    AccountWhereInput,
    AccountWhereUniqueInput,
    AccountUncheckedCreateInput,
    AccountUncheckedUpdateInput,
    AccountOrderByWithRelationInput,
    typeof ACCOUNT_RELATIONS
  >
  implements IAccountRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'account');
  }

  async findPaged(filter?: BaseFilter<AccountFilter>): Promise<Page<Account>> {
    const where = this.toFilterWhere(filter?.props);

    const [data, total] = await Promise.all([
      this.delegate.findMany({
        where,
        orderBy: { createdAt: 'desc' },
        include: {
          ...ACCOUNT_RELATIONS,
          transactions: filter?.props?.includeBalance ? true : false,
        },
        skip: (filter?.pageIndex ?? 0) * (filter?.pageSize ?? 1000),
        take: filter?.pageSize ?? 1000,
      }),
      this.delegate.count({ where }),
    ]);

    return new Page<Account>(
      data.map(m => AccountPrismaMapper.toAccountDomain(m)!),
      total,
      filter?.pageIndex ?? 0,
      filter?.pageSize ?? 1000,
    );
  }

  async findAll(filter?: AccountFilter): Promise<Account[]> {
    const accounts = await this.delegate.findMany({
      where: this.toFilterWhere(filter),
      orderBy: { createdAt: 'desc' },
      include: {
        ...ACCOUNT_RELATIONS,
        transactions: filter?.includeBalance ? true : false,
      },
    });

    return accounts.map(m => AccountPrismaMapper.toAccountDomain(m)!);
  }

  protected toFilterWhere(props?: AccountFilter): AccountWhereInput {
    const where: AccountWhereInput = {
      ...(props?.type && props.type.length > 0 ? { type: { in: [...props.type] } } : {}),
      ...(props?.ownerType && props.ownerType.length > 0 ? { ownerType: { in: [...props.ownerType] } } : {}),
      ...(props?.status && props.status.length > 0 ? { status: { in: [...props.status] } } : {}),
      ...(props?.accountHolderId === null ? { accountHolderId: null } :
        props?.accountHolderId ? { accountHolderId: props.accountHolderId } : {}),
      ...(props?.id ? { id: props.id } : {}),
      ...(props?.sourceAccountId
        ? { bankInvestDetail: { is: { sourceAccountId: props.sourceAccountId } } }
        : {}),
      deletedAt: null,
    };
    return where;
  }

  async findById(id: string): Promise<Account | null> {
    const account = await this.delegate.findUnique({
      where: { id },
      include: {
        ...ACCOUNT_RELATIONS,
        transactions: true,
      },
    });

    return AccountPrismaMapper.toAccountDomain(account!);
  }

  async create(account: Account): Promise<Account> {
    const createData: Prisma.AccountUncheckedCreateInput = {
      ...AccountPrismaMapper.toAccountCreatePersistence(account),
      transactions: {
        create: account.transactions.map(m => {
          const { accountId, ...createData } = TransactionPrismaMapper.toTransactionCreatePersistence(m);
          return createData;
        }),
      },
    };

    const created = await this.delegate.create({
      data: createData,
      include: {
        ...ACCOUNT_RELATIONS,
        transactions: true,
      },
    });

    return AccountPrismaMapper.toAccountDomain(created)!;
  }

  async update(id: string, account: Account, options?: { replaceUpiDetails?: boolean }): Promise<Account> {
    const updateData: Prisma.AccountUncheckedUpdateInput = {
      ...AccountPrismaMapper.toAccountUpdatePersistence(account, {
        replaceUpiDetails: options?.replaceUpiDetails,
      }),
      transactions: {
        upsert: account.transactions.map(m => {
          const { accountId, ...createData } = TransactionPrismaMapper.toTransactionCreatePersistence(m);
          return {
            where: { id: m.id },
            create: { ...createData, transactionRef: m.transactionRef },
            update: TransactionPrismaMapper.toTransactionUpdatePersistence(m),
          };
        }),
      },
    };

    const updated = await this.delegate.update({
      where: { id },
      data: updateData,
      include: {
        ...ACCOUNT_RELATIONS,
        transactions: true,
      },
    });

    return AccountPrismaMapper.toAccountDomain(updated)!;
  }

  protected toDomain(row: AccountWithRelations): Account {
    return AccountPrismaMapper.toAccountDomain(row)!;
  }

  protected toCreateInput(account: Account): AccountUncheckedCreateInput {
    return {
      ...AccountPrismaMapper.toAccountCreatePersistence(account),
      transactions: {
        create: account.transactions.map(m => {
          const { accountId, ...createData } = TransactionPrismaMapper.toTransactionCreatePersistence(m);
          return createData;
        }),
      },
    };
  }

  protected toUpdateInput(_id: string, account: Account): AccountUncheckedUpdateInput {
    return AccountPrismaMapper.toAccountUpdatePersistence(account);
  }

  protected toUniqueWhere(id: string): AccountWhereUniqueInput {
    return { id };
  }

  protected override supportsSoftDelete(): boolean {
    return true;
  }

  protected override defaultOrderBy(): AccountOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }
}
