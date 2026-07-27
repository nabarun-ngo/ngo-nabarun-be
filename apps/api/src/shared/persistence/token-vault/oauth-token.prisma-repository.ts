import { Injectable } from '@nestjs/common';
import {
  BasePrismaService,
  PrismaCrudRepositoryBase,
} from '@nabarun-ngo/nestjs-shared-persistence';
import { isEncryptedText } from '@nabarun-ngo/nestjs-shared-core';
import type { PrismaClient } from '../prisma/client';
import type {
  TokenVaultOAuthTokenWhereInput,
  TokenVaultOAuthTokenWhereUniqueInput,
  TokenVaultOAuthTokenCreateInput,
  TokenVaultOAuthTokenUncheckedCreateInput,
  TokenVaultOAuthTokenUpdateInput,
  TokenVaultOAuthTokenUncheckedUpdateInput,
  TokenVaultOAuthTokenOrderByWithRelationInput,
} from '../prisma/models/TokenVaultOAuthToken';
import {
  EncryptedToken,
  InvalidEncryptedTokenError,
  IOAuthTokenRepository,
  OAuthAccountSnapshot,
  OAuthToken,
  OAuthTokenFilter,
  TokenScope,
} from '@nabarun-ngo/nestjs-shared-token-vault';

/**
 * Extended row type that includes the eagerly-loaded account relation.
 * `toInclude()` returns `{ account: true }`, so all find methods automatically
 * eager-load the account, allowing `toDomain` to reconstruct the snapshot.
 */
type TokenRow = {
  id: string;
  accountId: string;
  clientId: string;
  provider: string;
  email: string;
  ownerSub: string | null;
  accessToken: string;
  refreshToken: string | null;
  tokenType: string | null;
  expiresAt: Date | null;
  scope: string | null;
  createdAt: Date;
  updatedAt: Date;
  account?: {
    id: string;
    email: string;
    externalId: string | null;
    name: string | null;
    givenName: string | null;
    familyName: string | null;
    pictureUrl: string | null;
    locale: string | null;
  } | null;
};

/**
 * Defensive guard: refuses to persist a token that was not properly encrypted.
 * Prevents plaintext tokens from reaching the database if the EncryptedToken
 * VO invariant was somehow bypassed.
 */
function assertEncrypted(token: OAuthToken): void {
  if (!isEncryptedText(token.accessToken.raw)) {
    throw new InvalidEncryptedTokenError();
  }
  if (token.refreshToken && !isEncryptedText(token.refreshToken.raw)) {
    throw new InvalidEncryptedTokenError();
  }
}

@Injectable()
export class OAuthTokenPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'tokenVaultOAuthToken',
    OAuthToken,
    string,
    OAuthTokenFilter,
    TokenRow,
    TokenVaultOAuthTokenWhereInput,
    TokenVaultOAuthTokenWhereUniqueInput,
    ({} & TokenVaultOAuthTokenUncheckedCreateInput) | ({} & TokenVaultOAuthTokenCreateInput),
    ({} & TokenVaultOAuthTokenUncheckedUpdateInput) | ({} & TokenVaultOAuthTokenUpdateInput),
    TokenVaultOAuthTokenOrderByWithRelationInput,
    { account: true }
  >
  implements IOAuthTokenRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'tokenVaultOAuthToken');
  }

  // ── toInclude hook — eager-loads account on every find query ─────────────

  protected override toInclude(): { account: true } {
    return { account: true };
  }

  // ── Custom interface methods ─────────────────────────────────────────────────

  async findByAttribute(filter: Partial<OAuthTokenFilter>): Promise<OAuthToken | null> {
    const row = await (this.delegate).findFirst({
      where: this.toFilterWhere(filter as OAuthTokenFilter),
      include: { account: true },
    });
    return row ? this.toDomain(row as TokenRow) : null;
  }

  // ── Mapping hooks ────────────────────────────────────────────────────────────

  protected toDomain(row: TokenRow): OAuthToken {
    const account: OAuthAccountSnapshot | undefined = row.account
      ? {
        id: row.account.id,
        email: row.account.email,
        externalId: row.account.externalId ?? undefined,
        name: row.account.name ?? undefined,
        givenName: row.account.givenName ?? undefined,
        familyName: row.account.familyName ?? undefined,
        pictureUrl: row.account.pictureUrl ?? undefined,
        locale: row.account.locale ?? undefined,
      }
      : undefined;

    return OAuthToken.rehydrate({
      id: row.id,
      accountId: row.accountId,
      clientId: row.clientId,
      provider: row.provider,
      email: row.email,
      ownerSub: row.ownerSub ?? undefined,
      accessToken: EncryptedToken.fromEncrypted(row.accessToken),
      refreshToken: row.refreshToken ? EncryptedToken.fromEncrypted(row.refreshToken) : null,
      tokenType: row.tokenType ?? undefined,
      expiresAt: row.expiresAt ?? undefined,
      scope: TokenScope.fromStorage(row.scope),
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
      account,
    });
  }

  protected toCreateInput(
    entity: OAuthToken,
  ): ({} & TokenVaultOAuthTokenUncheckedCreateInput) | ({} & TokenVaultOAuthTokenCreateInput) {
    assertEncrypted(entity);
    return {
      id: entity.id,
      accountId: entity.accountId,
      clientId: entity.clientId,
      provider: entity.provider,
      email: entity.email,
      ownerSub: entity.ownerSub ?? null,
      accessToken: entity.accessToken.raw,
      refreshToken: entity.refreshToken?.raw ?? null,
      tokenType: entity.tokenType ?? null,
      expiresAt: entity.expiresAt ?? null,
      scope: entity.scope?.toString() ?? null,
      createdAt: entity.createdAt,
      updatedAt: entity.updatedAt,
    };
  }

  protected toUpdateInput(
    _id: string,
    entity: OAuthToken,
  ): ({} & TokenVaultOAuthTokenUncheckedUpdateInput) | ({} & TokenVaultOAuthTokenUpdateInput) {
    assertEncrypted(entity);
    return {
      accessToken: entity.accessToken.raw,
      ...(entity.refreshToken ? { refreshToken: entity.refreshToken.raw } : {}),
      expiresAt: entity.expiresAt ?? null,
      tokenType: entity.tokenType ?? null,
      // Persist scope and ownerSub: refresh() may update these on re-authorisation.
      scope: entity.scope?.toString() ?? null,
      ownerSub: entity.ownerSub ?? null,
      updatedAt: entity.updatedAt,
    };
  }

  protected toUniqueWhere(id: string): TokenVaultOAuthTokenWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(filter?: OAuthTokenFilter): TokenVaultOAuthTokenWhereInput {
    return {
      ...(filter?.provider ? { provider: filter.provider } : {}),
      ...(filter?.email ? { email: filter.email } : {}),
      ...(filter?.clientId ? { clientId: filter.clientId } : {}),
      ...(filter?.ownerSub ? { ownerSub: filter.ownerSub } : {}),
      ...(filter?.scope ? { scope: { contains: filter.scope } } : {}),
    };
  }

  protected defaultOrderBy(): TokenVaultOAuthTokenOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected supportsSoftDelete(): boolean {
    return false;
  }
}
