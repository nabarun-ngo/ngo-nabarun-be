


type PermissionOp = 'C' | 'R' | 'U' | 'D';

type PermissionDef = {
    key: string;
    type: PermissionOp;
    description: string;
};

type PermissionArea =
    | 'custom-forms'
    | 'custom-forms-submission'
    | 'api-keys'
    | 'auth-definitions'
    | 'auth-management'
    | 'job-queue'
    | 'requests'
    | 'json-documents'
    | 'dms'
    | 'comment'
    | 'cron'
    | 'users'
    | 'donation'
    | 'donor'
    | 'accounts'
    | 'expenses'
    | 'earnings'
    | 'reports'
    | 'correspondence-notifications'
    | 'correspondence-email'
    | 'correspondence-subscriptions'
    | 'help-portal'
    | 'token-vault'
    | 'project'
    | 'beneficiary'
    | 'goal'
    | 'milestone'
    | 'project-team'
    | 'project-risk'
    | 'meeting'
    | 'asset'
    | 'book-bank'
    | 'public-site';

export const PermissionMap: Record<PermissionArea, PermissionDef[]> = {
    'custom-forms': [
        { key: 'create:custom_forms', type: 'C', description: 'Create custom forms' },
        { key: 'read:custom_forms', type: 'R', description: 'View custom form definitions' },
        { key: 'update:custom_forms', type: 'U', description: 'Update custom forms and fields' },
        { key: 'disable:custom_forms', type: 'D', description: 'Disable custom forms and fields' },
        { key: 'delete:custom_forms', type: 'D', description: 'Delete custom forms' },// Future Not in use
    ],
    'custom-forms-submission': [
        { key: 'read:form_submissions', type: 'R', description: 'Read form submission values' },
        { key: 'create:form_submissions', type: 'C', description: 'Save draft/submit form submission values' },
        { key: 'write:form_submissions', type: 'C', description: 'Write draft form submission values' },
        { key: 'submit:form_submissions', type: 'C', description: 'Submit form submission values' },
        { key: 'clear:form_submissions', type: 'D', description: 'Clear form submission values' },
        { key: 'delete:form_submissions', type: 'D', description: 'Clear form submission values' },
    ],
    'api-keys': [
        { key: 'read:api_keys', type: 'R', description: 'List API keys and their scopes' },
        { key: 'create:api_keys', type: 'C', description: 'Generate a new API key' },
        { key: 'update:api_keys', type: 'U', description: 'Update permissions on an existing API key' },
        { key: 'delete:api_keys', type: 'D', description: 'Revoke an API key' },
    ],
    'auth-definitions': [
        // ── rbac roles ────────────────────────────────────────────────────────────────
        { key: 'read:roles', type: 'R', description: 'View all RBAC roles' },
        { key: 'create:roles', type: 'C', description: 'Create RBAC roles' },
        { key: 'update:roles', type: 'U', description: 'Update RBAC roles and their permission mappings' },
        { key: 'delete:roles', type: 'D', description: 'Soft-delete RBAC roles' },
        // ── rbac permissions ────────────────────────────────────────────────────────────────
        { key: 'read:permissions', type: 'R', description: 'View all registered permissions' },
        { key: 'create:permissions', type: 'C', description: 'Create RBAC permissions' },
        { key: 'update:permissions', type: 'U', description: 'Update RBAC permissions' },
        { key: 'delete:permissions', type: 'D', description: 'Soft-delete RBAC permissions' },
        // ── rbac role groups ────────────────────────────────────────────────────────────────
        { key: 'read:role_groups', type: 'R', description: 'View all role groups' },
        { key: 'create:role_groups', type: 'C', description: 'Create RBAC role groups' },
        { key: 'update:role_groups', type: 'U', description: 'Update RBAC role groups and their role mappings' },
        { key: 'delete:role_groups', type: 'D', description: 'Soft-delete RBAC role groups' },
    ],
    'auth-management': [
        // ── rbac user roles ────────────────────────────────────────────────────────────────
        { key: 'read:user_roles', type: 'R', description: 'View roles and role-group memberships of any user' },
        { key: 'create:user_roles', type: 'C', description: 'Grant a role or add a user to a role group' },
        { key: 'delete:user_roles', type: 'D', description: 'Revoke a role or remove a user from a role group' },
        // ── rbac user permissions ────────────────────────────────────────────────────────────────
        { key: 'read:user_permissions', type: 'R', description: 'View direct permission grants of any user' },
        { key: 'create:user_permissions', type: 'C', description: 'Grant a permission directly to a user' },
        { key: 'delete:user_permissions', type: 'D', description: 'Revoke a direct permission grant from a user' },
        // ── rbac user role groups ────────────────────────────────────────────────────────────────
        { key: 'read:user_role_groups', type: 'R', description: 'View role-group memberships of any user' },
        { key: 'create:user_role_groups', type: 'C', description: 'Add a user to a role group' },
        { key: 'delete:user_role_groups', type: 'D', description: 'Remove a user from a role group' },
    ],
    'job-queue': [
        // ── queue ────────────────────────────────────────────────────────────────
        { key: 'read:jobs', type: 'R', description: 'View background job status' },
        { key: 'update:jobs', type: 'U', description: 'Retry or update background jobs' },
        { key: 'delete:jobs', type: 'D', description: 'Remove background jobs from the queue' },
    ],
    'requests': [
        { key: 'create:requests', type: 'C', description: 'Create and start requests instances' },
        { key: 'read:requests', type: 'R', description: 'View requests timelines' },
        { key: 'update:requests', type: 'U', description: 'Cancel or update requests instances' },
        { key: 'read:tasks', type: 'R', description: 'View assigned workflow tasks (inbox)' },
        { key: 'update:task', type: 'U', description: 'Claim, complete, or delegate workflow tasks' },
        { key: 'admin:workflows', type: 'U', description: 'Administrative workflow operations (force-skip, stuck detector)' },
        { key: 'manage:workflow_definitions', type: 'U', description: 'Publish and manage workflow definitions' },
    ],
    'json-documents': [
        { key: 'create:json_documents', type: 'C', description: 'Create json_documents' },
        { key: 'read:json_documents', type: 'R', description: 'View json_documents' },
        { key: 'update:json_documents', type: 'U', description: 'Update json_documents' },
        { key: 'delete:json_documents', type: 'D', description: 'Delete json_documents' },
    ],
    'dms': [
        // ── dms ────────────────────────────────────────────────────────────────
        { key: 'read:documents', type: 'R', description: 'View document references' },
        { key: 'create:documents', type: 'C', description: 'Upload or attach documents' },
        { key: 'update:documents', type: 'U', description: 'Update document metadata' },
        { key: 'delete:documents', type: 'D', description: 'Delete document references' },
    ],
    'comment': [
        { key: 'read:comments', type: 'R', description: 'View comments' },
        { key: 'create:comments', type: 'C', description: 'Create comments' },
        { key: 'update:comments', type: 'U', description: 'Update comments' },
        { key: 'delete:comments', type: 'D', description: 'Delete comments' },
        { key: 'read:donation_comments', type: 'R', description: 'Read comments on donation entities' },
        { key: 'create:donation_comments', type: 'C', description: 'Post comments on donation entities' },
        { key: 'read:task_comments', type: 'R', description: 'Read comments on task entities' },
        { key: 'create:task_comments', type: 'C', description: 'Post comments on task entities' },
    ],
    'cron': [
        { key: 'read:cron', type: 'R', description: 'View cron job definitions' },
        { key: 'create:cron', type: 'C', description: 'Create cron job definitions' },
        { key: 'update:cron', type: 'U', description: 'Update cron job definitions' },
        { key: 'delete:cron', type: 'D', description: 'Delete cron job definitions' },
    ],
    'users': [
        // ── user (consumer-defined) ──────────────────────────────────────────────
        { key: 'create:users', type: 'C', description: 'Create user profiles' },
        { key: 'read:users', type: 'R', description: 'Read user profiles' },
        { key: 'update:users', type: 'U', description: 'Update user profiles' },
        { key: 'delete:users', type: 'D', description: 'Delete user profiles' },
        { key: 'create:user_connections', type: 'C', description: 'Create user connections' },
        { key: 'read:user_connections', type: 'R', description: 'Read user connections' },
        { key: 'delete:user_connections', type: 'D', description: 'Delete user connections' },
        { key: 'create:identity_cards', type: 'C', description: 'Create identity cards' },
        { key: 'read:identity_cards', type: 'R', description: 'Read identity cards' },
    ],
    'donation': [
        // ── donations (consumer-defined) ─────────────────────────────────────────
        { key: 'read:donations', type: 'R', description: 'View donation records' },
        { key: 'create:donation', type: 'C', description: 'Create member donation' },
        { key: 'create:donation_guest', type: 'C', description: 'Create guest donation' },
        { key: 'update:donation', type: 'U', description: 'Update donation details or payment status' },
        { key: 'read:member_donations', type: 'R', description: 'View donations for a specific member' },
        { key: 'read:donation_guest', type: 'R', description: 'View guest donations' },
    ],
    'donor': [
        { key: 'read:donors', type: 'R', description: 'View donor records' },
        { key: 'create:donor_guest', type: 'C', description: 'Create guest donor profiles' },
        { key: 'update:donor_guest', type: 'U', description: 'Update guest donor profiles' },
        { key: 'update:donor_member', type: 'U', description: 'Update member donor schedule and amount' },
        { key: 'merge:donor_guest', type: 'U', description: 'Merge guest donor profiles' },
    ],
    'accounts': [
        // ── accounts / transactions (consumer-defined) ───────────────────────────
        { key: 'create:account', type: 'C', description: 'Create financial account' },
        { key: 'update:account', type: 'U', description: 'Update financial account details' },
        { key: 'read:accounts', type: 'R', description: 'View financial accounts' },
        { key: 'read:transactions', type: 'R', description: 'View account transactions' },
        { key: 'update:accounts', type: 'U', description: 'Adjust account balances' },
        { key: 'update:transactions', type: 'U', description: 'Create or reverse transactions' },
    ],
    'expenses': [
        // ── expenses (consumer-defined) ──────────────────────────────────────────
        { key: 'create:expense', type: 'C', description: 'Create expense record' },
        { key: 'read:expenses', type: 'R', description: 'View expense records' },
        { key: 'update:expense', type: 'U', description: 'Update expense records' },
        { key: 'finalize:expense', type: 'U', description: 'Finalize (approve) expense' },
        { key: 'settle:expense', type: 'U', description: 'Settle (pay) expense' },
        { key: 'delete:expense', type: 'D', description: 'Delete expense records' },
    ],
    'earnings': [
        { key: 'create:earning', type: 'C', description: 'Create earning record' },
        { key: 'read:earnings', type: 'R', description: 'View earning records' },
        { key: 'update:earning', type: 'U', description: 'Update earning records' },
        { key: 'delete:earning', type: 'D', description: 'Delete earning records' },
    ],
    'reports': [
        // ── reports (consumer-defined) ──────────────────────────────────────────
        { key: 'read:reports', type: 'R', description: 'View report definitions and executions' },
        { key: 'create:reports', type: 'C', description: 'Generate reports' },
        { key: 'update:reports', type: 'U', description: 'Update reports' },
        { key: 'approve:reports', type: 'U', description: 'Approve reports via workflow tasks' },
        { key: 'delete:reports', type: 'D', description: 'Delete report executions' },
    ],
    'correspondence-notifications': [
        { key: 'read:notifications', type: 'R', description: 'View own notifications and notification admin listings' },
        { key: 'update:notifications', type: 'U', description: 'Mark or update own notification state' },
    ],
    'correspondence-email': [
        { key: 'send:email', type: 'C', description: 'Send email via the correspondence provider' },
    ],
    'correspondence-subscriptions': [
        { key: 'read:subscriptions', type: 'R', description: 'List own or resource correspondence subscriptions' },
        { key: 'create:subscriptions', type: 'C', description: 'Follow a resource (create correspondence subscription)' },
        { key: 'update:subscriptions', type: 'U', description: 'Update subscription channel preferences' },
        { key: 'delete:subscriptions', type: 'D', description: 'Unfollow a resource (deactivate subscription)' },
    ],
    'help-portal': [
        { key: 'read:help_portal', type: 'R', description: 'View in-app help catalog and articles' },
    ],
    'token-vault': [
        { key: 'read:oauth_token', type: 'R', description: 'View OAuth token records' },
        { key: 'create:oauth_token', type: 'C', description: 'Create or refresh OAuth tokens' },
        { key: 'delete:oauth_token', type: 'D', description: 'Revoke OAuth tokens' },
    ],
    'project': [
        { key: 'read:projects', type: 'R', description: 'View project records' },
        { key: 'create:project', type: 'C', description: 'Create projects' },
        { key: 'update:project', type: 'U', description: 'Update projects' },
        { key: 'read:activities', type: 'R', description: 'View project activities' },
        { key: 'create:activity', type: 'C', description: 'Create project activities' },
        { key: 'update:activity', type: 'U', description: 'Update project activities' },
    ],
    'beneficiary': [
        { key: 'read:beneficiaries', type: 'R', description: 'View project beneficiaries' },
        { key: 'create:beneficiary', type: 'C', description: 'Create project beneficiaries' },
        { key: 'update:beneficiary', type: 'U', description: 'Update project beneficiaries' },
    ],
    'goal': [
        { key: 'read:goals', type: 'R', description: 'View project goals' },
        { key: 'create:goal', type: 'C', description: 'Create project goals' },
        { key: 'update:goal', type: 'U', description: 'Update project goals' },
    ],
    'milestone': [
        { key: 'read:milestones', type: 'R', description: 'View project milestones' },
        { key: 'create:milestone', type: 'C', description: 'Create project milestones' },
        { key: 'update:milestone', type: 'U', description: 'Update project milestones' },
    ],
    'project-team': [
        { key: 'read:project_teams', type: 'R', description: 'View project team members' },
        { key: 'create:project_team', type: 'C', description: 'Add project team members' },
        { key: 'update:project_team', type: 'U', description: 'Update project team members' },
    ],
    'project-risk': [
        { key: 'read:risks', type: 'R', description: 'View project risks' },
        { key: 'create:risk', type: 'C', description: 'Create project risks' },
        { key: 'update:risk', type: 'U', description: 'Update project risks' },
    ],
    'meeting': [
        { key: 'read:meetings', type: 'R', description: 'View meeting records' },
        { key: 'create:meeting', type: 'C', description: 'Schedule meetings and sync with Google Calendar' },
        { key: 'update:meeting', type: 'U', description: 'Update or cancel meeting details' },
        { key: 'delete:meeting', type: 'D', description: 'Delete meeting records' },
    ],
    'asset': [
        { key: 'read:assets', type: 'R', description: 'View physical asset records' },
        { key: 'create:asset', type: 'C', description: 'Register physical assets' },
        { key: 'update:asset', type: 'U', description: 'Update assets and assign or return custody' },
        { key: 'delete:asset', type: 'D', description: 'Soft-delete physical asset records' },
    ],
    'book-bank': [
        { key: 'read:books', type: 'R', description: 'View book bank records' },
        { key: 'create:book', type: 'C', description: 'Register books in the book bank' },
        { key: 'update:book', type: 'U', description: 'Update books and apply lend/return/donate operations' },
        { key: 'delete:book', type: 'D', description: 'Soft-delete book bank records' },
    ],
    'public-site': [
        { key: 'read:public_content', type: 'R', description: 'Read public site content' },
    ],
};

function applyPermissions(area: PermissionArea, types: PermissionOp[] = ['R']) {
    return PermissionMap[area].filter(p => types.includes(p.type)).map(p => p.key);
}

function extendMember(...extras: string[]): Set<string> {
    return new Set([...memberPermissions, ...extras]);
}

export const memberPermissions: Set<string> = new Set([
    ...applyPermissions('custom-forms', []),
    ...applyPermissions('custom-forms-submission', ['R', 'C', 'D']),
    ...applyPermissions('api-keys', []),
    ...applyPermissions('auth-definitions', ['R']),
    ...applyPermissions('auth-management', ['R']),
    ...applyPermissions('job-queue', []),
    ...applyPermissions('requests', ['C', 'R']),
    ...applyPermissions('json-documents', ['R']),
    ...applyPermissions('dms', ['R', 'C']),
    ...applyPermissions('comment', ['R', 'C', 'U', 'D']),
    ...applyPermissions('cron', []),
    ...applyPermissions('users', ['R']),
    ...applyPermissions('donation', ['R', 'U']),
    ...applyPermissions('donor', ['R']),
    ...applyPermissions('accounts', ['R']),
    ...applyPermissions('expenses', ['R']),
    ...applyPermissions('earnings', ['R']),
    ...applyPermissions('reports', ['R']),
    ...applyPermissions('correspondence-notifications', ['R', 'U']),
    ...applyPermissions('correspondence-email', []),
    ...applyPermissions('correspondence-subscriptions', ['R', 'C', 'U', 'D']),
    ...applyPermissions('help-portal', ['R']),
    ...applyPermissions('token-vault', []),
    ...applyPermissions('project', ['R']),
    ...applyPermissions('beneficiary', ['R']),
    ...applyPermissions('goal', ['R']),
    ...applyPermissions('milestone', ['R']),
    ...applyPermissions('project-team', ['R']),
    ...applyPermissions('project-risk', ['R']),
    ...applyPermissions('meeting', ['R']),
    ...applyPermissions('asset', ['R']),
    ...applyPermissions('book-bank', ['R']),
    ...applyPermissions('public-site', ['R']),
]);

const governanceRecordExtras = [
    ...applyPermissions('auth-definitions', ['C', 'R', 'U', 'D']),
    ...applyPermissions('auth-management', ['C', 'R', 'D']),
    ...applyPermissions('dms', ['C', 'R', 'U', 'D']),
    ...applyPermissions('asset', ['C', 'R', 'U', 'D']),
    ...applyPermissions('book-bank', ['C', 'R', 'U', 'D']),
    ...applyPermissions('meeting', ['C', 'R', 'U', 'D']),
    ...applyPermissions('reports', ['C', 'R', 'U', 'D']),
];

const financeWriteExtras = [
    ...applyPermissions('donation', ['C', 'R', 'U']),
    ...applyPermissions('donor', ['C', 'R', 'U']),
    ...applyPermissions('accounts', ['C', 'R', 'U']),
    ...applyPermissions('expenses', ['C', 'R', 'U']),
    ...applyPermissions('earnings', ['C', 'R', 'U']),
];

export const secretaryPermissions = extendMember(
    ...governanceRecordExtras,
    ...applyPermissions('users', ['C', 'R', 'U', 'D']),
);

export const assistantSecretaryPermissions = extendMember(
    ...governanceRecordExtras,
    ...applyPermissions('users', ['C', 'R', 'U']),
);

export const treasurerPermissions = extendMember(
    ...applyPermissions('dms', ['C', 'R', 'U', 'D']),
    ...applyPermissions('asset', ['C', 'R', 'U', 'D']),
    ...applyPermissions('book-bank', ['C', 'R', 'U', 'D']),
    ...applyPermissions('reports', ['C', 'R', 'U', 'D']),
    ...financeWriteExtras,
    ...applyPermissions('users', ['R', 'U']),
);

export const communityManagerPermissions = extendMember(
    ...applyPermissions('asset', ['C', 'R', 'U', 'D']),
    ...applyPermissions('book-bank', ['C', 'R', 'U', 'D']),
);
 



