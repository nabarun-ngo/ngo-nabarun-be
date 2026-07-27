import { Injectable } from '@nestjs/common';
import {
  BasePrismaService,
  PrismaCrudRepositoryBase,
} from '@nabarun-ngo/nestjs-shared-persistence';
import type { PrismaClient } from '../../prisma/client';
import type {
  TokenVaultOAuthAccountWhereInput,
  TokenVaultOAuthAccountWhereUniqueInput,
  TokenVaultOAuthAccountCreateInput,
  TokenVaultOAuthAccountUncheckedCreateInput,
  TokenVaultOAuthAccountUpdateInput,
  TokenVaultOAuthAccountUncheckedUpdateInput,
  TokenVaultOAuthAccountOrderByWithRelationInput,
} from '../../prisma/models/TokenVaultOAuthAccount';
import {
  OAuthAccount,
  OAuthAccountFilter,
  IOAuthAccountRepository,
} from '@nabarun-ngo/nestjs-shared-token-vault';

type AccountRow = {
  id: string;
  provider: string;
  email: string;
  externalId: string | null;
  name: string | null;
  givenName: string | null;
  familyName: string | null;
  pictureUrl: string | null;
  locale: string | null;
  createdAt: Date;
  updatedAt: Date;
  deletedAt: Date | null;
};

@Injectable()
export class OAuthAccountPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'tokenVaultOAuthAccount',
    OAuthAccount,
    string,
    OAuthAccountFilter,
    AccountRow,
    TokenVaultOAuthAccountWhereInput,
    TokenVaultOAuthAccountWhereUniqueInput,
    ({} & TokenVaultOAuthAccountUncheckedCreateInput) | ({} & TokenVaultOAuthAccountCreateInput),
    ({} & TokenVaultOAuthAccountUncheckedUpdateInput) | ({} & TokenVaultOAuthAccountUpdateInput),
    TokenVaultOAuthAccountOrderByWithRelationInput
  >
  implements IOAuthAccountRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'tokenVaultOAuthAccount');
  }

  async findByProviderAndEmail(provider: string, email: string): Promise<OAuthAccount | null> {
    // HIGH-6: findUnique cannot carry extra conditions (like deletedAt: null) on a
    // composite unique key in Prisma, so we use findFirst with an explicit soft-delete
    // guard to prevent returning logically-deleted accounts.
    const row = await (this.delegate).findFirst({
      where: { provider, email, deletedAt: null },
    });
    return row ? this.toDomain(row as AccountRow) : null;
  }

  protected toDomain(row: AccountRow): OAuthAccount {
    return OAuthAccount.rehydrate({
      id: row.id,
      provider: row.provider,
      email: row.email,
      externalId: row.externalId ?? undefined,
      name: row.name ?? undefined,
      givenName: row.givenName ?? undefined,
      familyName: row.familyName ?? undefined,
      pictureUrl: row.pictureUrl ?? undefined,
      locale: row.locale ?? undefined,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    });
  }

  protected toCreateInput(
    entity: OAuthAccount,
  ): ({} & TokenVaultOAuthAccountUncheckedCreateInput) | ({} & TokenVaultOAuthAccountCreateInput) {
    return {
      id: entity.id,
      provider: entity.provider,
      email: entity.email,
      externalId: entity.externalId ?? null,
      name: entity.name ?? null,
      givenName: entity.givenName ?? null,
      familyName: entity.familyName ?? null,
      pictureUrl: entity.pictureUrl ?? null,
      locale: entity.locale ?? null,
      createdAt: entity.createdAt,
      updatedAt: entity.updatedAt,
    };
  }

  protected toUpdateInput(
    _id: string,
    entity: OAuthAccount,
  ): ({} & TokenVaultOAuthAccountUncheckedUpdateInput) | ({} & TokenVaultOAuthAccountUpdateInput) {
    return {
      email: entity.email,
      externalId: entity.externalId ?? null,
      name: entity.name ?? null,
      givenName: entity.givenName ?? null,
      familyName: entity.familyName ?? null,
      pictureUrl: entity.pictureUrl ?? null,
      locale: entity.locale ?? null,
      updatedAt: entity.updatedAt,
    };
  }

  protected toUniqueWhere(id: string): TokenVaultOAuthAccountWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(filter?: OAuthAccountFilter): TokenVaultOAuthAccountWhereInput {
    return {
      ...(filter?.provider ? { provider: filter.provider } : {}),
      ...(filter?.email ? { email: filter.email } : {}),
    };
  }

  protected defaultOrderBy(): TokenVaultOAuthAccountOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected supportsSoftDelete(): boolean {
    return true;
  }
}
