
// Comma-separated IdP subs to auto-assign to the SUPER_ADMINS group.

import { AuthSeedData } from "./auth-seed.types";
import {
  PermissionMap,
  assistantSecretaryPermissions,
  communityManagerPermissions,
  memberPermissions,
  secretaryPermissions,
  treasurerPermissions,
} from "./permission-map.data";

// Example .env entry:  SEED_SUPER_ADMIN_IDP_SUBS=auth0|abc123,auth0|def456
const superAdminSubs = (process.env.SEED_SUPER_ADMIN_IDP_SUBS ?? '')
  .split(',')
  .map((s) => s.trim())
  .filter(Boolean);

// ─────────────────────────────────────────────────────────────────────────────

export const AUTH_SEED: AuthSeedData = {
  permissions: [
    ...Object.values(PermissionMap).flatMap(perm => perm.map(p => ({
      key: p.key,
      description: p.description
    })))
  ],

  roles: [
    // ── Base role ─────────────────────────────────────────────────────────────
    {
      key: 'MEMBER',
      description: 'Base role — auto-assigned to every new user on registration. Basic read-heavy access with self-service writes (requests, comments, subscriptions).',
      permissionKeys: [...memberPermissions],
    },

    // ── Governance roles ──────────────────────────────────────────────────────
    {
      key: 'PRESIDENT',
      description: 'Highest authority. Same permission set as Member; additional access comes from other assigned roles or groups.',
      permissionKeys: [...memberPermissions],
    },
    {
      key: 'VICE_PRESIDENT',
      description: 'Second in command. Same permission set as Member; additional access comes from other assigned roles or groups.',
      permissionKeys: [...memberPermissions],
    },
    {
      key: 'SECRETARY',
      description: 'Administrative officer. Member access plus records, communications, and member data.',
      permissionKeys: [...secretaryPermissions],
    },
    {
      key: 'ASSISTANT_SECRETARY',
      description: 'Junior administrative officer. Member access plus limited write on records and members.',
      permissionKeys: [...assistantSecretaryPermissions],
    },
    {
      key: 'TREASURER',
      description: 'Financial officer. Member access plus donation, account, expense, and earning management.',
      permissionKeys: [...treasurerPermissions],
    },
    {
      key: 'COMMUNITY_MANAGER',
      description: 'Manages community outreach and social media platforms. Member access plus asset and book-bank write.',
      permissionKeys: [...communityManagerPermissions],
    },
  ],

  roleGroups: [
    // ── Governance groups (displayable office-bearer bundles) ────────────────
    {
      key: 'EXECUTIVE_BOARD',
      description: 'Top leadership — President and Vice President.',
      roleKeys: ['PRESIDENT', 'VICE_PRESIDENT'],
    },
    {
      key: 'SECRETARIAT',
      description: 'Administrative body — Secretary and Assistant Secretary.',
      roleKeys: ['SECRETARY', 'ASSISTANT_SECRETARY'],
    },
    {
      key: 'FINANCE_TEAM',
      description: 'Financial oversight — Treasurer.',
      roleKeys: ['TREASURER'],
    },
    {
      key: 'GOVERNING_COMMITTEE',
      description: 'Full elected governing body — all office bearers.',
      roleKeys: ['PRESIDENT', 'VICE_PRESIDENT', 'SECRETARY', 'ASSISTANT_SECRETARY', 'TREASURER'],
    },

    // ── Shadow groups (isShadow: true — not in member role-group picker) ─────
    {
      key: 'PLATFORM_ADMINS',
      description: 'Shadow group — technical platform operators (TECH_ADMIN).',
      isShadow: true,
      roleKeys: ['TECH_ADMIN'],
    },
    {
      key: 'SUPER_ADMINS',
      description:
        'Shadow group — break-glass full access. Membership seeded from SEED_SUPER_ADMIN_IDP_SUBS; grants every seeded role.',
      isShadow: true,
      roleKeys: [
        'PRESIDENT',
        'VICE_PRESIDENT',
        'SECRETARY',
        'ASSISTANT_SECRETARY',
        'TREASURER',
        'COMMUNITY_MANAGER',
        'TECH_ADMIN',
        'MEMBER',
      ],
      seedUsers: superAdminSubs,
    },
  ],
};
