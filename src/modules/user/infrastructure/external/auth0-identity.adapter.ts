import { Inject, Injectable, Logger } from '@nestjs/common';
import { AuthenticationClient, ManagementClient } from 'auth0';
import { User } from '../../domain/aggregates/user/user.aggregate';
import {
  GrantConnectionResult,
  IIdentityProvider,
  IdentityCreateOptions,
  IdentityCreateResult,
  IdentityUserPatch,
  LinkedConnection,
  PasswordChangeTicketOptions,
  PasswordChangeTicketResult,
} from '../../domain/ports/identity-provider.port';
import { IdentityProviderError, InvalidCredentialsError } from '../../domain/errors/user.errors';
import { USER_OPTIONS } from '../user-options.token';
import type { ConnectionConfig, ConnectionType, UserModuleOptions } from '../../user.schema';

/** Shape of an Auth0 identity object returned from users.get. */
interface Auth0Identity {
  connection: string;
  provider: string;
  user_id: string;
  isSocial?: boolean;
}

@Injectable()
export class Auth0IdentityAdapter implements IIdentityProvider {
  private readonly logger = new Logger(Auth0IdentityAdapter.name);
  private readonly management: ManagementClient;
  private readonly authentication: AuthenticationClient;

  constructor(
    @Inject(USER_OPTIONS) private readonly options: UserModuleOptions,
  ) {
    const { domain, clientId, clientSecret } = options.idp;
    this.management = new ManagementClient({ domain, clientId, clientSecret });
    // SPA client — Auth0 Change Password emails and ticket redirects use this app.
    this.authentication = new AuthenticationClient({
      domain,
      clientId: options.spaClientId,
    });
  }

  // ── createUser ────────────────────────────────────────────────────────────

  /**
   * 1. Create the user in the `default` (primary) connection with a strong
   *    system-generated password (never returned) and `email_verified: false`.
   * 2. For every other connection where `provisionOnCreate: true` and type is
   *    `password` or `passwordless`, create a secondary identity and link it.
   * 3. Return the primary `externalSub`.
   */
  async createUser(
    user: User,
    options: IdentityCreateOptions = {},
  ): Promise<IdentityCreateResult> {
    const primary = this.resolveConnection('default');
    const primarySub = await this.createInConnection(user, primary, options);

    const secondaries = Object.entries(this.options.idp.connections).filter(
      ([key, conn]) =>
        key !== 'default' &&
        conn.provisionOnCreate &&
        this.isProvisionable(conn.type),
    );

    for (const [, conn] of secondaries) {
      try {
        const secondarySub = await this.createInConnection(user, conn, {
          emailVerified: options.emailVerified,
        });
        await this.linkIdentity(primarySub, secondarySub);
      } catch (err) {
        this.logger.warn(
          `Failed to provision secondary connection '${conn.name}' for user ${user.email}: ` +
          (err instanceof Error ? err.message : String(err)),
        );
      }
    }

    return { externalSub: primarySub };
  }

  // ── updateUser ────────────────────────────────────────────────────────────

  async updateUser(externalSub: string, patch: IdentityUserPatch): Promise<void> {
    try {
      await this.management.users.update(externalSub, {
        ...(patch.firstName ? { given_name: patch.firstName } : {}),
        ...(patch.lastName ? { family_name: patch.lastName } : {}),
        ...(patch.firstName && patch.lastName
          ? { name: `${patch.firstName} ${patch.lastName}` }
          : {}),
        ...(patch.picture ? { picture: patch.picture } : {}),
      });
    } catch (err) {
      throw this.wrapError('updateUser', err);
    }
  }

  // ── deleteUser ────────────────────────────────────────────────────────────

  async deleteUser(externalSub: string): Promise<void> {
    try {
      await this.management.users.delete(externalSub);
    } catch (err) {
      throw this.wrapError('deleteUser', err);
    }
  }

  // ── createPasswordChangeTicket ────────────────────────────────────────────

  /**
   * POST /api/v2/tickets/password-change
   * @see https://auth0.com/docs/api/management/v2/tickets/post-password-change
   */
  async createPasswordChangeTicket(
    options: PasswordChangeTicketOptions,
  ): Promise<PasswordChangeTicketResult> {
    try {
      const res = await this.management.tickets.changePassword({
        user_id: options.userId,
        client_id: this.options.spaClientId,
        mark_email_as_verified: options.markEmailAsVerified ?? true,
        includeEmailInRedirect: options.includeEmailInRedirect ?? true,
        ...(options.ttlSec != null ? { ttl_sec: options.ttlSec } : {}),
        ...(options.resultUrl ? { result_url: options.resultUrl } : {}),
      });
      const ticketUrl = res['ticket'];
      if (!ticketUrl) {
        throw new IdentityProviderError('Password-change ticket response missing ticket URL');
      }
      return { ticketUrl };
    } catch (err) {
      throw this.wrapError('createPasswordChangeTicket', err);
    }
  }

  // ── verifyPassword ────────────────────────────────────────────────────────

  /**
   * Authentication API Resource Owner Password Grant (login).
   * @see https://auth0.com/docs/api/authentication#resource-owner-password
   */
  async verifyPassword(email: string, password: string): Promise<void> {
    const realm = this.resolveConnection('default').name;
    try {
      await this.authentication.oauth.passwordGrant({
        username: email,
        password,
        realm,
        scope: 'openid',
      });
    } catch (err) {
      this.logger.debug(
        `Password verification failed for ${email}: ${err instanceof Error ? err.message : err}`,
      );
      throw new InvalidCredentialsError();
    }
  }

  // ── sendPasswordReset ─────────────────────────────────────────────────────

  /**
   * Authentication API — Auth0 sends its Change Password email template.
   * Only used by self-service reset; onboarding delivers the ticket URL itself.
   * @see https://auth0.com/docs/api/authentication/change-password
   */
  private async sendPasswordChangeEmail(email: string): Promise<void> {
    try {
      const connection = this.resolveConnection('default').name;
      await this.authentication.database.changePassword({
        email,
        connection,
      });
    } catch (err) {
      throw this.wrapError('sendPasswordChangeEmail', err);
    }
  }

  async sendPasswordReset(externalSub: string): Promise<void> {
    const authUser = await this.fetchUser(externalSub, 'sendPasswordReset');
    const email = (authUser as { email?: string }).email
      ?? (authUser as { data?: { email?: string } }).data?.email;
    if (!email) throw new IdentityProviderError(`No email found for user ${externalSub}`);

    const identities: Auth0Identity[] =
      (authUser as { identities?: Auth0Identity[] }).identities
      ?? (authUser as { data?: { identities?: Auth0Identity[] } }).data?.identities
      ?? [];

    for (const identity of identities) {
      const conn = this.findConnectionByName(identity.connection);
      if (!conn) continue;

      if (conn.type === 'password') {
        await this.sendPasswordChangeEmail(email).catch((err: unknown) =>
          this.logger.warn(
            `Password email failed for ${identity.connection}: ${err instanceof Error ? err.message : err}`,
          ),
        );
      } else if (conn.type === 'passwordless') {
        const userId = `${identity.provider}|${identity.user_id}`;
        await this.sendVerificationEmail(userId).catch((err: unknown) =>
          this.logger.warn(
            `Passwordless re-send failed for ${identity.connection}: ${err instanceof Error ? err.message : err}`,
          ),
        );
      }
    }
  }

  // ── grantConnection ───────────────────────────────────────────────────────

  async grantConnection(
    externalSub: string,
    connectionKey: string,
    user: User,
  ): Promise<GrantConnectionResult> {
    const conn = this.resolveConnection(connectionKey);

    if (!this.isProvisionable(conn.type)) {
      throw new IdentityProviderError(
        `Cannot grant connection '${connectionKey}' (type '${conn.type}'). ` +
        `Only 'password' and 'passwordless' connections can be granted via the API. ` +
        `Social and enterprise identities are provisioned externally on first OAuth/federated login.`,
      );
    }

    try {
      const secondarySub = await this.createInConnection(user, conn, { emailVerified: false });
      await this.linkIdentity(externalSub, secondarySub);
      return { status: 'linked' };
    } catch (err) {
      throw this.wrapError('grantConnection', err);
    }
  }

  // ── revokeConnection ──────────────────────────────────────────────────────

  async revokeConnection(externalSub: string, connectionKey: string): Promise<void> {
    if (connectionKey === 'default') {
      throw new IdentityProviderError(
        `The primary connection ('default') cannot be revoked. Delete the user instead.`,
      );
    }
    const conn = this.resolveConnection(connectionKey);
    const authUser = await this.fetchUser(externalSub, 'revokeConnection');

    const identities: Auth0Identity[] =
      (authUser as { identities?: Auth0Identity[] }).identities
      ?? (authUser as { data?: { identities?: Auth0Identity[] } }).data?.identities
      ?? [];
    const identity = identities.find((id) => id.connection === conn.name);
    if (!identity) {
      throw new IdentityProviderError(
        `User does not have a linked identity for connection '${connectionKey}' (${conn.name}).`,
      );
    }

    try {
      await this.management.users.identities.delete(
        externalSub,
        identity.provider as never,
        identity.user_id,
      );
    } catch (err) {
      throw this.wrapError('revokeConnection', err);
    }
  }

  // ── listConnections ───────────────────────────────────────────────────────

  async listConnections(externalSub: string): Promise<LinkedConnection[]> {
    const authUser = await this.fetchUser(externalSub, 'listConnections');
    const primaryConnectionName = this.resolveConnection('default').name;
    const identities: Auth0Identity[] =
      (authUser as { identities?: Auth0Identity[] }).identities
      ?? (authUser as { data?: { identities?: Auth0Identity[] } }).data?.identities
      ?? [];

    return identities.map((identity) => {
      const entry = this.findConnectionEntryByName(identity.connection);
      return {
        connectionKey: entry?.key ?? '__unknown__',
        connectionName: identity.connection,
        type: (entry?.conn.type ?? 'social') as ConnectionType,
        provider: identity.provider,
        isPrimary: identity.connection === primaryConnectionName && !identity.isSocial,
      };
    });
  }

  // ── Private helpers ───────────────────────────────────────────────────────

  private async fetchUser(externalSub: string, operation: string) {
    try {
      return await this.management.users.get(externalSub);
    } catch (err) {
      throw this.wrapError(`${operation}.getUser`, err);
    }
  }

  /**
   * Create a user in a single Auth0 connection and return the full `user_id`.
   * No optional app_metadata is written on create.
   */
  private async createInConnection(
    user: User,
    conn: ConnectionConfig,
    options: IdentityCreateOptions,
  ): Promise<string> {
    const emailVerified = options.emailVerified ?? false;
    const base = {
      email: user.email,
      given_name: user.firstName,
      family_name: user.lastName,
      name: user.fullName,
      connection: conn.name,
      email_verified: emailVerified,
    };

    if (conn.type === 'password') {
      const password = this.generateCompliantPassword();
      const res = await this.management.users.create({
        ...base,
        password,
      });
      return this.extractUserId(res);
    }

    // passwordless — no password field, no metadata
    const res = await this.management.users.create({ ...base });
    const userId = this.extractUserId(res);
    if (!emailVerified) {
      await this.sendVerificationEmail(userId);
    }
    return userId;
  }

  private extractUserId(res: unknown): string {
    const id =
      (res as { user_id?: string }).user_id
      ?? (res as { data?: { user_id?: string } }).data?.user_id;
    if (!id) {
      throw new IdentityProviderError('Auth0 user create response missing user_id');
    }
    return id;
  }

  private async linkIdentity(primarySub: string, secondaryFullId: string): Promise<void> {
    const [provider, ...rest] = secondaryFullId.split('|');
    const userId = rest.join('|');
    await this.management.users.identities.link(primarySub, {
      provider: provider as never,
      user_id: userId,
    });
  }

  private async sendVerificationEmail(userId: string): Promise<void> {
    await this.management.jobs.verificationEmail.create({ user_id: userId });
  }

  private resolveConnection(key: string): ConnectionConfig {
    const config = this.options.idp.connections[key];
    if (!config) {
      throw new IdentityProviderError(
        `Unknown connection key '${key}'. ` +
        `Available: ${Object.keys(this.options.idp.connections).join(', ')}.`,
      );
    }
    return config;
  }

  private findConnectionByName(connectionName: string): ConnectionConfig | undefined {
    return this.findConnectionEntryByName(connectionName)?.conn;
  }

  private findConnectionEntryByName(
    connectionName: string,
  ): { key: string; conn: ConnectionConfig } | undefined {
    const entry = Object.entries(this.options.idp.connections).find(
      ([, conn]) => conn.name === connectionName,
    );
    return entry ? { key: entry[0], conn: entry[1] } : undefined;
  }

  private isProvisionable(type: ConnectionConfig['type']): boolean {
    return type === 'password' || type === 'passwordless';
  }

  /**
   * Strong password for Auth0 Fair/Good policies: 24+ chars with upper, lower,
   * digit, and special. Never logged or returned to callers.
   */
  private generateCompliantPassword(): string {
    const upper = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
    const lower = 'abcdefghijklmnopqrstuvwxyz';
    const digits = '0123456789';
    const special = '!@#$%^&*()-_=+[]{}';
    const all = upper + lower + digits + special;
    const rand = (set: string) => set[Math.floor(Math.random() * set.length)];
    const required = [rand(upper), rand(lower), rand(digits), rand(special)];
    const extra = Array.from({ length: 28 }, () => rand(all));
    return [...required, ...extra].sort(() => Math.random() - 0.5).join('');
  }

  private wrapError(operation: string, err: unknown): IdentityProviderError {
    const message = err instanceof Error ? err.message : `Auth0 ${operation} failed`;
    this.logger.error(`Auth0 ${operation} error: ${message}`, err);
    return new IdentityProviderError(`Identity provider error during ${operation}: ${message}`);
  }
}
