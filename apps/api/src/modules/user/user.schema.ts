import { z } from 'zod';

/**
 * Supported Auth0 connection types and their provisioning behaviour.
 *
 * - `password`     — Auth0 database connection. Pre-provisioned with a strong
 *                    system-generated password (never emailed). Set-password uses
 *                    a Management API change-password ticket.
 * - `passwordless` — Auth0 `email` or `sms` connection. Pre-provisioned without
 *                    a password. User completes first login via magic-link/OTP.
 * - `social`       — OAuth2 connection (Google, GitHub, Facebook, Apple, etc.).
 *                    Cannot be pre-provisioned. Identity created on first OAuth login.
 * - `enterprise`   — Federated connection (SAML, OIDC, Entra ID, ADFS).
 *                    Cannot be pre-provisioned. Identity created on first federated login.
 */
export const ConnectionType = {
  Password:     'password',
  Passwordless: 'passwordless',
  //Social:       'social',
  //Enterprise:   'enterprise',
} as const;
export type ConnectionType = (typeof ConnectionType)[keyof typeof ConnectionType];

const ConnectionConfigSchema = z.object({
  /** The Auth0 connection name passed to the Management API. */
  name: z.string().min(1),
  /** Determines how the identity is provisioned and what reset flow applies. */
  type: z.enum(['password', 'passwordless', /*'social', 'enterprise'*/]).default('password'),
  /**
   * When true, this connection is automatically provisioned and linked when a new
   * user is created. Only meaningful for `password` and `passwordless` types —
   * social and enterprise connections cannot be pre-provisioned via the Management API.
   */
  provisionOnCreate: z.boolean().default(true),
});

export type ConnectionConfig = z.infer<typeof ConnectionConfigSchema>;

/**
 * Named connection map. The `default` key is required (primary identity).
 * Additional keys map logical names to Auth0 connection configs.
 *
 * @example
 * connections: {
 *   default:      { name: 'Username-Password-Authentication', type: 'password',     provisionOnCreate: true  },
 *   passwordless: { name: 'email',                            type: 'passwordless', provisionOnCreate: true  },
 *   google:       { name: 'google-oauth',                    type: 'social',       provisionOnCreate: false },
 *   saml:         { name: 'samlp',                            type: 'enterprise',   provisionOnCreate: false },
 * }
 */
const ConnectionsSchema = z
  .object({ default: ConnectionConfigSchema })
  .catchall(ConnectionConfigSchema);

const IdpOptionsSchema = z.object({
  /** Auth0 tenant domain, e.g. `my-tenant.us.auth0.com`. */
  domain: z.string().min(1),
  /** Management API client ID. */
  clientId: z.string().min(1),
  /** Management API client secret. */
  clientSecret: z.string().min(1),
  /** Named connection map. Defaults to a single password-based connection. */
  connections: ConnectionsSchema.default({
    default: { name: 'Username-Password-Authentication', type: 'password', provisionOnCreate: true },
  }),
});

export type IdpOptions = z.infer<typeof IdpOptionsSchema>;

export const UserModuleOptionsSchema = z.object({
  /** Auth0 Management API credentials and connection configuration. */
  idp: IdpOptionsSchema,
  /**
   * SPA / Universal Login application client ID.
   * Passed as `client_id` on password-change tickets so Auth0 can redirect to the
   * application's default login route after set-password.
   */
  spaClientId: z.string().min(1),
  /**
   * Frontend app URL used as `result_url` on self-service password-change tickets
   * so Auth0 can redirect back after the hosted reset completes.
   */
  appFeUrl: z.string().url(),
  /** Public API origin used on identity-card QR codes (verify URL). */
  publicApiUrl: z.string().url(),
  /** Organisation name printed on the identity card header. */
  organisationName: z.string().min(1).default('Member'),
  /** Society/NGO registration number printed under the organisation name. */
  organisationRegistrationNumber: z.string().min(1).optional(),
  /**
   * Optional organisation logo on the identity card header.
   * Must be a `data:image/(jpeg|jpg|png|webp);base64,...` value. Remote http(s)
   * URLs are stripped so the printer never fetches an external asset.
   */
  organisationLogoDataUrl: z
    .string()
    .optional()
    .transform((value) => {
      const trimmed = value?.trim();
      if (!trimmed) return undefined;
      if (/^https?:/i.test(trimmed)) return undefined;
      if (!/^data:image\/(jpeg|jpg|png|webp);base64,/i.test(trimmed)) return undefined;
      return trimmed;
    }),
  /** Default role keys to grant on admin user create (via Auth GrantUserRoleCommand). */
  defaultRoleKeys: z.array(z.string()).optional().default([]),
});

/** Parsed/output type — all defaults applied. Used internally after schema.parse(). */
export type UserModuleOptions = z.infer<typeof UserModuleOptionsSchema>;

/** Raw input type — fields with defaults are optional. Use as the factory return type in forRootAsync(). */
export type UserModuleInput = z.input<typeof UserModuleOptionsSchema>;
