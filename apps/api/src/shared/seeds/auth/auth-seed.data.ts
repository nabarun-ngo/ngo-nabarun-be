
// Comma-separated IdP subs to auto-assign to the SUPER_ADMINS group.

import { AuthSeedData } from "./auth-seed.types";
import { PermissionMap, permissions } from "./permission-map.data";

// Example .env entry:  SEED_SUPER_ADMIN_IDP_SUBS=auth0|abc123,auth0|def456
const superAdminSubs = (process.env.SEED_SUPER_ADMIN_IDP_SUBS ?? '')
  .split(',')
  .map((s) => s.trim())
  .filter(Boolean);


const MEMBER_PERMISSION = [
  'read:permissions',
  'read:roles',
  'read:role_groups',
  'read:user_roles',
  'read:user_permissions',
] as const;

// ─── Canonical permission key sets (re-used in multiple roles) ───────────────

const RBAC_READ = [
  'read:roles',
  'read:permissions',
  'read:role_groups',
  'read:user_roles',
  'read:user_permissions',
] as const;

const RBAC_CATALOG_WRITE = [
  'create:permissions',
  'update:permissions',
  'delete:permissions',
  'create:roles',
  'update:roles',
  'delete:roles',
  'create:role_groups',
  'update:role_groups',
  'delete:role_groups',
] as const;

const RBAC_MANAGE = [
  ...RBAC_READ,
  'create:user_roles',
  'delete:user_roles',
  'create:user_permissions',
  'delete:user_permissions',
  ...RBAC_CATALOG_WRITE,
] as const;


const API_KEYS_ALL = [
  'read:api_keys',
  'create:api_keys',
  'update:api_keys',
  'delete:api_keys',
] as const;

const QUEUE_PERMISSIONS_ALL = [
  'read:jobs',
  'update:jobs',
  'delete:jobs',
];

const JSON_STORE_ALL = [
  'read:json_documents',
  'create:json_documents',
  'update:json_documents',
  'delete:json_documents',
] as const;

const DOCS_READ_ALL = [
  'read:documents',
] as const;

const DOCS_CREATE = [
  'create:documents',
] as const;

const DOCS_ALL = [
  ...DOCS_READ_ALL,
  ...DOCS_CREATE,
  'update:documents',
  'delete:documents',
] as const;

const CUSTOM_FORMS_DEFINITIONS_ALL = [
  'read:custom_forms',
  'create:custom_forms',
  'update:custom_forms',
  'disable:custom_forms',
] as const;

const CUSTOM_FORMS_SUBMISSIONS_ALL = [
  'read:form_submissions',
  'write:form_submissions',
  'submit:form_submissions',
  'clear:form_submissions',
] as const;

const DONATIONS_ALL = [
  'read:donations',
  'read:donation_comments',
  'create:donation_comments',
  'create:donation',
  'create:donation_guest',
  'update:donation',
  'read:member_donations',
  'read:donation_guest',
  'read:donors',
  'create:donor_guest',
  'update:donor_guest',
  'update:donor_member',
  'merge:donor_guest',
] as const;

const ACCOUNTS_ALL = [
  'create:account',
  'update:account',
  'read:accounts',
  'read:transactions',
  'update:accounts',
  'update:transactions',
] as const;

const EXPENSES_ALL = [
  'create:expense',
  'update:expense',
  'finalize:expense',
  'settle:expense',
  'read:expenses',
] as const;

const EARNINGS_ALL = [
  'create:earning',
  'update:earning',
  'read:earnings',
] as const;

const PROJECT_ALL = [
  'read:projects',
  'create:project',
  'update:project',
  'read:activities',
  'create:activity',
  'update:activity',
  'read:beneficiaries',
  'create:beneficiary',
  'update:beneficiary',
  'read:goals',
  'create:goal',
  'update:goal',
  'read:milestones',
  'create:milestone',
  'update:milestone',
  'read:project_teams',
  'create:project_team',
  'update:project_team',
  'read:risks',
  'create:risk',
  'update:risk',
] as const;

const FINANCE_ALL = [
  ...DONATIONS_ALL,
  ...ACCOUNTS_ALL,
  ...EXPENSES_ALL,
  ...EARNINGS_ALL,
] as const;

const FINANCE_GRANULAR = [
  'create:donation',
  'create:donation_guest',
  'update:donation',
  'read:member_donations',
  'read:donation_guest',
  'read:donors',
  'create:donor_guest',
  'update:donor_guest',
  'update:donor_member',
  'merge:donor_guest',
  'create:account',
  'update:account',
  'read:accounts',
  'read:transactions',
  'update:accounts',
  'update:transactions',
  'create:expense',
  'update:expense',
  'finalize:expense',
  'settle:expense',
  'read:expenses',
  'create:earning',
  'update:earning',
  'read:earnings',
] as const;

const WORKFLOW_ALL = [
  'create:requests',
  'read:requests',
  'update:requests',
  'read:tasks',
  'update:task',
  'admin:workflows',
  'manage:workflow_definitions',
] as const;

const REPORTS_ALL = [
  'read:reports',
  'create:reports',
  'delete:reports',
  'approve:reports',
] as const;

const MEETING_ALL = [
  'read:meetings',
  'create:meeting',
  'update:meeting',
  'delete:meeting',
] as const;

const ASSET_ALL = [
  'read:assets',
  'create:asset',
  'update:asset',
  'delete:asset',
] as const;

const BOOK_BANK_ALL = [
  'read:books',
  'create:book',
  'update:book',
  'delete:book',
] as const;

const CRON_ALL = [
  'read:cron',
  'update:cron',
] as const;

const OAUTH_TOKEN_ALL = [
  'read:oauth_token',
  'create:oauth_token',
  'delete:oauth_token',
] as const;

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
      description: 'Base role — auto-assigned to every new user on registration. No elevated permissions.',
      permissionKeys: [
        ...RBAC_READ,
        //DMS Create and Read
        ...DOCS_CREATE,
        ...DOCS_READ_ALL,
        'create:requests',
        'read:requests',
        'read:tasks',
        'read:member_donations',
        'update:donation',
        'read:help_portal',
        'read:meetings',
        'read:assets',
        'read:books',
        'read:notifications',
        'update:notifications',
        'read:subscriptions',
        'create:subscriptions',
        'update:subscriptions',
        'delete:subscriptions',
        ...CUSTOM_FORMS_SUBMISSIONS_ALL,
        'read:users',
        'read:user_connections',

      ],
    },

    // ── Governance roles ──────────────────────────────────────────────────────
    {
      key: 'PRESIDENT',
      description: 'Highest authority. Full governance and administrative access.',
      permissionKeys: [
        ...RBAC_MANAGE,
        ...DOCS_ALL,
        ...ASSET_ALL,
        ...BOOK_BANK_ALL,
        'update:users',
        'read:user_connections',
        'create:identity_cards',
        'read:identity_cards',

      ],
    },
    {
      key: 'VICE_PRESIDENT',
      description: 'Second in command. Broad governance access.',
      permissionKeys: [
        ...RBAC_MANAGE,
        ...DOCS_ALL,
        ...ASSET_ALL,
        ...BOOK_BANK_ALL,
        'update:users',
        'read:user_connections',
        'create:identity_cards',
        'read:identity_cards',

      ],
    },
    {
      key: 'SECRETARY',
      description: 'Administrative officer. Manages records, communications, and member data.',
      permissionKeys: [
        ...RBAC_MANAGE,
        ...DOCS_ALL,
        ...ASSET_ALL,
        ...BOOK_BANK_ALL,
        'create:users',
        'update:users',
        'delete:users',
        'read:user_connections',
        'create:identity_cards',
        'read:identity_cards',
      ],
    },
    {
      key: 'ASSISTANT_SECRETARY',
      description: 'Junior administrative officer. Read-heavy access with limited write capabilities.',
      permissionKeys: [
        ...RBAC_MANAGE,
        ...DOCS_ALL,
        ...ASSET_ALL,
        ...BOOK_BANK_ALL,
        'create:users',
        'update:users',
        'read:user_connections',
        'create:identity_cards',
        'read:identity_cards',
      ],
    },
    {
      key: 'TREASURER',
      description: 'Financial officer. Manages and reports on donation and financial records.',
      permissionKeys: [
        ...DOCS_ALL,
        ...ASSET_ALL,
        ...BOOK_BANK_ALL,
        'update:users',
        'read:user_connections',

      ],
    },
    {
      key: 'COMMUNITY_MANAGER',
      description: 'Manages community outreach and social media platforms.',
      permissionKeys: [
        ...ASSET_ALL,
        ...BOOK_BANK_ALL,
        'read:public_content',
      ],
    },

    // ── Shadow / platform roles (isShadow: true — not in member role picker) ─
    {
      key: 'TECH_ADMIN',
      description: 'Shadow role — technical administrator for platform infrastructure. Not assignable from the member role picker.',
      isShadow: true,
      permissionKeys: [
        ...API_KEYS_ALL,
        ...JSON_STORE_ALL,
        ...QUEUE_PERMISSIONS_ALL,
        ...DOCS_ALL,
        ...CUSTOM_FORMS_DEFINITIONS_ALL,
        ...CRON_ALL,
        ...OAUTH_TOKEN_ALL,
        ...RBAC_CATALOG_WRITE,
        ...ASSET_ALL,
        ...BOOK_BANK_ALL,
        'read:roles',
        'read:permissions',
        'read:role_groups',
        'create:user_connections',
        'delete:user_connections',
        'create:users',
        'update:users',
        'delete:users',
        'send:email',
        'create:identity_cards',
        'read:identity_cards',
      ],
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
