import { User } from '../aggregates/user/user.aggregate';
import type { ConnectionType } from '../../user.schema';

export const IIdentityProvider = Symbol('IIdentityProvider');

// ── Create ────────────────────────────────────────────────────────────────────

export interface IdentityCreateOptions {
  /**
   * When true, Auth0 `email_verified` is set on create.
   * Admin provisioning always uses `false` — verification happens via the
   * set-password ticket (`mark_email_as_verified`).
   */
  emailVerified?: boolean;
}

export interface IdentityCreateResult {
  /** Opaque IdP subject → stored as `UserProfile.idpSub`. */
  externalSub: string;
}

export interface PasswordChangeTicketOptions {
  /** Auth0 user_id / externalSub. */
  userId: string;
  /**
   * When true, Auth0 sets `email_verified` after the user completes the ticket.
   * Defaults to true for admin provisioning / invite flows.
   */
  markEmailAsVerified?: boolean;
  /**
   * When true, include the email in the redirect URL after reset.
   * Defaults to true.
   */
  includeEmailInRedirect?: boolean;
  /**
   * Ticket lifetime in seconds. Auth0 default is 5 days when omitted / 0.
   */
  ttlSec?: number;
  /**
   * Classic Universal Login redirect after the ticket is used.
   * New Universal Login prefers `client_id` → application default login route.
   */
  resultUrl?: string;
}

export interface PasswordChangeTicketResult {
  /** Hosted Auth0 set-password URL (Management API does not email this). */
  ticketUrl: string;
}

// ── Update ────────────────────────────────────────────────────────────────────

export interface IdentityUserPatch {
  firstName?: string;
  lastName?: string;
  picture?: string;
}

// ── Connections ───────────────────────────────────────────────────────────────

/**
 * Result of a grantConnection call.
 * Only `password` and `passwordless` connections can be granted — the identity is
 * created and linked synchronously. Social and enterprise connections are not supported
 * (they are provisioned externally on first OAuth/federated login).
 */
export type GrantConnectionResult = { status: 'linked' };

export interface LinkedConnection {
  /** Logical key from the `idp.connections` map, or `__unknown__` when unmapped. */
  connectionKey: string;
  /** Raw Auth0 connection name (e.g. `google-oauth`, `email`). */
  connectionName: string;
  type: ConnectionType;
  /** Auth0 identity provider string (e.g. `auth0`, `google-oauth`, `email`). */
  provider: string;
  /** True for the identity that owns the stored `externalSub`. */
  isPrimary: boolean;
}

// ── Port ──────────────────────────────────────────────────────────────────────

export interface IIdentityProvider {
  /**
   * Provision the user in all configured connections with `provisionOnCreate: true`
   * and link them via Auth0 account linking. Returns the primary identity's `externalSub`.
   * Password connections always receive a strong system-generated password that is
   * never returned or emailed.
   */
  createUser(user: User, options?: IdentityCreateOptions): Promise<IdentityCreateResult>;

  /** Sync name / picture to Auth0 after profile update. */
  updateUser(externalSub: string, patch: IdentityUserPatch): Promise<void>;

  /** Soft-delete complement: removes the user from Auth0. */
  deleteUser(externalSub: string): Promise<void>;

  /**
   * Create a Management API password-change ticket
   * (`POST /api/v2/tickets/password-change`) with SPA `client_id`,
   * `mark_email_as_verified`, and `includeEmailInRedirect`.
   * Auth0 does not email the ticket, so the URL is returned and delivered by our
   * own onboarding correspondence.
   */
  createPasswordChangeTicket(
    options: PasswordChangeTicketOptions,
  ): Promise<PasswordChangeTicketResult>;

  /**
   * Verify the user's current database password via Authentication API
   * Resource Owner Password Grant (`oauth.passwordGrant` / login).
   * Resolves on success; throws `InvalidCredentialsError` when Auth0 rejects.
   */
  verifyPassword(email: string, password: string): Promise<void>;

  /**
   * Self-service reset for an existing member. Iterates every linked identity:
   * - `password` connection   → Auth0 change-password email (Authentication API)
   * - `passwordless` connection → verification email (re-sends magic-link)
   * Social / enterprise identities are skipped (no server-side reset possible).
   * Not part of onboarding — new members get the ticket URL in the welcome email.
   */
  sendPasswordReset(externalSub: string): Promise<void>;

  /**
   * Grant a new `password` or `passwordless` connection to an existing user.
   * Creates the secondary identity in Auth0 and links it to the primary via account linking.
   * Throws `IdentityProviderError` if the connection type is `social` or `enterprise`
   * (those identities are provisioned externally on first OAuth / federated login).
   */
  grantConnection(
    externalSub: string,
    connectionKey: string,
    user: User,
  ): Promise<GrantConnectionResult>;

  /**
   * Unlink a secondary identity from the user.
   * Throws if `connectionKey` resolves to the primary (`default`) connection.
   */
  revokeConnection(externalSub: string, connectionKey: string): Promise<void>;

  /**
   * List all Auth0 identities currently linked to the user, enriched with
   * the logical connection key from the `idp.connections` map.
   */
  listConnections(externalSub: string): Promise<LinkedConnection[]>;
}
