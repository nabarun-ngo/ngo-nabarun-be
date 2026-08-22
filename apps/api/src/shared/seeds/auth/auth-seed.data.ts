
// Comma-separated IdP subs to auto-assign to the SUPER_ADMINS group.

import { AuthSeedData } from "./auth-seed.types";

// Example .env entry:  SEED_SUPER_ADMIN_IDP_SUBS=auth0|abc123,auth0|def456
const superAdminSubs = (process.env.SEED_SUPER_ADMIN_IDP_SUBS ?? '')
  .split(',')
  .map((s) => s.trim())
  .filter(Boolean);

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
    // ── auth (self) ────────────────────────────────────────────────────────
    { key: 'read:roles', description: 'View all RBAC roles' },
    { key: 'read:permissions', description: 'View all registered permissions' },
    { key: 'read:role_groups', description: 'View all role groups' },
    { key: 'read:user_roles', description: 'View roles and role-group memberships of any user' },
    { key: 'create:user_roles', description: 'Grant a role or add a user to a role group' },
    { key: 'delete:user_roles', description: 'Revoke a role or remove a user from a role group' },
    { key: 'read:user_permissions', description: 'View direct permission grants of any user' },
    { key: 'create:user_permissions', description: 'Grant a permission directly to a user' },
    { key: 'delete:user_permissions', description: 'Revoke a direct permission grant from a user' },
    { key: 'create:roles', description: 'Create RBAC roles' },
    { key: 'update:roles', description: 'Update RBAC roles and their permission mappings' },
    { key: 'delete:roles', description: 'Soft-delete RBAC roles' },
    { key: 'create:permissions', description: 'Create RBAC permissions' },
    { key: 'update:permissions', description: 'Update RBAC permissions' },
    { key: 'delete:permissions', description: 'Soft-delete RBAC permissions' },
    { key: 'create:role_groups', description: 'Create RBAC role groups' },
    { key: 'update:role_groups', description: 'Update RBAC role groups and their role mappings' },
    { key: 'delete:role_groups', description: 'Soft-delete RBAC role groups' },
    // --- API Keys Management
    { key: 'read:api_keys', description: 'List API keys and their scopes' },
    { key: 'create:api_keys', description: 'Generate a new API key' },
    { key: 'update:api_keys', description: 'Update permissions on an existing API key' },
    { key: 'delete:api_keys', description: 'Revoke an API key' },

    // ── json-store ───────────────────────────────────────────────────────────
    { key: 'read:json_documents', description: 'Read JSON store documents' },
    { key: 'create:json_documents', description: 'Create JSON store documents' },
    { key: 'update:json_documents', description: 'Update JSON store documents' },
    { key: 'delete:json_documents', description: 'Delete JSON store documents' },

    // ── correspondence ──────────────────────────────────────────────────────
    { key: 'read:notifications', description: 'Access notification admin endpoints' },
    { key: 'update:notifications', description: 'Mark or update own notification state' },
    { key: 'send:email', description: 'Send email via the correspondence provider' },
    { key: 'read:subscriptions', description: 'List own or resource correspondence subscriptions' },
    { key: 'create:subscriptions', description: 'Follow a resource (create correspondence subscription)' },
    { key: 'update:subscriptions', description: 'Update subscription channel preferences' },
    { key: 'delete:subscriptions', description: 'Unfollow a resource (deactivate subscription)' },

    // ── cron ────────────────────────────────────────────────────────────────
    { key: 'read:cron', description: 'View cron job definitions and status' },
    { key: 'update:cron', description: 'Trigger or update cron job definitions' },

    // ── help portal ───────────────────────────────────────────────────────────
    { key: 'read:help_portal', description: 'View in-app help catalog and articles' },

    // ── dms ────────────────────────────────────────────────────────────────
    { key: 'read:documents', description: 'View document references' },
    { key: 'create:documents', description: 'Upload or attach documents' },
    { key: 'update:documents', description: 'Update document metadata' },
    { key: 'delete:documents', description: 'Delete document references' },

    // ── reports ─────────────────────────────────────────────────────────────
    { key: 'read:reports', description: 'View report definitions and executions' },
    { key: 'create:reports', description: 'Generate reports' },
    { key: 'delete:reports', description: 'Delete report executions' },
    { key: 'approve:reports', description: 'Approve reports via workflow tasks' },

    // ── custom-forms ─────────────────────────────────────────────────────────
    { key: 'read:custom_forms', description: 'View custom form definitions' },
    { key: 'create:custom_forms', description: 'Create custom forms' },
    { key: 'update:custom_forms', description: 'Update custom forms and fields' },
    { key: 'disable:custom_forms', description: 'Disable custom forms and fields' },
    { key: 'read:form_submissions', description: 'Read form submission values' },
    { key: 'write:form_submissions', description: 'Save draft form submission values' },
    { key: 'submit:form_submissions', description: 'Submit form submissions' },
    { key: 'clear:form_submissions', description: 'Clear form submission values' },

    // ── token-vault ─────────────────────────────────────────────────────────
    { key: 'read:oauth_token', description: 'View OAuth token records' },
    { key: 'create:oauth_token', description: 'Create or refresh OAuth tokens' },
    { key: 'delete:oauth_token', description: 'Revoke OAuth tokens' },

    // ── queue ────────────────────────────────────────────────────────────────
    { key: 'read:jobs', description: 'View background job status' },
    { key: 'update:jobs', description: 'Retry or update background jobs' },
    { key: 'delete:jobs', description: 'Remove background jobs from the queue' },

    // ── user (consumer-defined) ──────────────────────────────────────────────
    { key: 'create:users', description: 'Create user profiles' },
    { key: 'read:users', description: 'Read user profiles' },
    { key: 'update:users', description: 'Update user profiles' },
    { key: 'delete:users', description: 'Delete user profiles' },
    { key: 'create:user_connections', description: 'Create user connections' },
    { key: 'read:user_connections', description: 'Read user connections' },
    { key: 'delete:user_connections', description: 'Delete user connections' },

    // ── comment entity-type permissions (consumer-defined) ──────────────────
    { key: 'read:donation_comments', description: 'Read comments on donation entities' },
    { key: 'create:donation_comments', description: 'Post comments on donation entities' },
    { key: 'read:task_comments', description: 'Read comments on task entities' },
    { key: 'create:task_comments', description: 'Post comments on task entities' },

    // ── donations (consumer-defined) ─────────────────────────────────────────
    { key: 'read:donations', description: 'View donation records' },
    { key: 'create:donation', description: 'Create member donation' },
    { key: 'create:donation_guest', description: 'Create guest donation' },
    { key: 'update:donation', description: 'Update donation details or payment status' },
    { key: 'read:member_donations', description: 'View donations for a specific member' },
    { key: 'read:donation_guest', description: 'View guest donations' },
    { key: 'read:donors', description: 'View donor records' },
    { key: 'create:donor_guest', description: 'Create guest donor profiles' },
    { key: 'update:donor_guest', description: 'Update guest donor profiles' },
    { key: 'update:donor_member', description: 'Update member donor schedule and amount' },
    { key: 'merge:donor_guest', description: 'Merge duplicate guest donor records' },

    // ── accounts / transactions (consumer-defined) ───────────────────────────
    { key: 'create:account', description: 'Create financial account' },
    { key: 'update:account', description: 'Update financial account details' },
    { key: 'read:accounts', description: 'View financial accounts' },
    { key: 'read:transactions', description: 'View account transactions' },
    { key: 'update:accounts', description: 'Adjust account balances' },
    { key: 'update:transactions', description: 'Create or reverse transactions' },

    // ── expenses (consumer-defined) ──────────────────────────────────────────
    { key: 'create:expense', description: 'Create expense record' },
    { key: 'update:expense', description: 'Update expense record' },
    { key: 'finalize:expense', description: 'Finalize (approve) expense' },
    { key: 'settle:expense', description: 'Settle (pay) expense' },
    { key: 'read:expenses', description: 'View expense records' },

    // ── earnings (consumer-defined) ──────────────────────────────────────────
    { key: 'create:earning', description: 'Create earning record' },
    { key: 'update:earning', description: 'Update earning record' },
    { key: 'read:earnings', description: 'View earning records' },

    // ── project (consumer-defined) ───────────────────────────────────────────
    { key: 'read:projects', description: 'View project records' },
    { key: 'create:project', description: 'Create projects' },
    { key: 'update:project', description: 'Update projects' },
    { key: 'read:activities', description: 'View project activities' },
    { key: 'create:activity', description: 'Create project activities' },
    { key: 'update:activity', description: 'Update project activities' },
    { key: 'read:beneficiaries', description: 'View project beneficiaries' },
    { key: 'create:beneficiary', description: 'Create project beneficiaries' },
    { key: 'update:beneficiary', description: 'Update project beneficiaries' },
    { key: 'read:goals', description: 'View project goals' },
    { key: 'create:goal', description: 'Create project goals' },
    { key: 'update:goal', description: 'Update project goals' },
    { key: 'read:milestones', description: 'View project milestones' },
    { key: 'create:milestone', description: 'Create project milestones' },
    { key: 'update:milestone', description: 'Update project milestones' },
    { key: 'read:project_teams', description: 'View project team members' },
    { key: 'create:project_team', description: 'Add project team members' },
    { key: 'update:project_team', description: 'Update project team members' },
    { key: 'read:risks', description: 'View project risks' },
    { key: 'create:risk', description: 'Create project risks' },
    { key: 'update:risk', description: 'Update project risks' },

    // ── requests / workflow (consumer-defined) ───────────────────────────────
    { key: 'create:requests', description: 'Create and start requests / workflow instances' },
    { key: 'read:requests', description: 'View requests and workflow timelines' },
    { key: 'update:requests', description: 'Cancel or update requests / workflow instances' },
    { key: 'read:tasks', description: 'View assigned workflow tasks (inbox)' },
    { key: 'update:task', description: 'Claim, complete, or delegate workflow tasks' },
    { key: 'admin:workflows', description: 'Administrative workflow operations (force-skip, stuck detector)' },
    { key: 'manage:workflow_definitions', description: 'Publish and manage workflow definitions' },

    // ── meeting (consumer-defined) ───────────────────────────────────────────
    { key: 'read:meetings', description: 'View meeting records' },
    { key: 'create:meeting', description: 'Schedule meetings and sync with Google Calendar' },
    { key: 'update:meeting', description: 'Update or cancel meeting details' },
    { key: 'delete:meeting', description: 'Delete meeting records' },

    // ── asset (consumer-defined) ─────────────────────────────────────────────
    { key: 'read:assets', description: 'View physical asset records' },
    { key: 'create:asset', description: 'Register physical assets' },
    { key: 'update:asset', description: 'Update assets and assign or return custody' },
    { key: 'delete:asset', description: 'Soft-delete physical asset records' },

    // ── book bank (library under assets hub) ─────────────────────────────────
    { key: 'read:books', description: 'View book bank records' },
    { key: 'create:book', description: 'Register books in the book bank' },
    { key: 'update:book', description: 'Update books and apply lend/return/donate operations' },
    { key: 'delete:book', description: 'Soft-delete book bank records' },

    // ── public site ───────────────────────────────────────────────────────────
    { key: 'read:public_content', description: 'Read public site content' },
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
