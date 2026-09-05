



export const permissions =
    [




    ];

export const PermissionMap: Record<string, { key: string, type: 'C' | 'R' | 'U' | 'D', description: string }[]> = {
    'custom-forms': [
        { key: 'create:custom_forms', type: 'C', description: 'Create custom forms' },
        { key: 'read:custom_forms', type: 'R', description: 'View custom form definitions' },
        { key: 'update:custom_forms', type: 'U', description: 'Update custom forms and fields' },
        { key: 'delete:custom_forms', type: 'D', description: 'Delete custom forms' },// Future Not in use
    ],
    'custom-forms-submission': [
        { key: 'read:form_submissions', type: 'R', description: 'Read form submission values' },
        { key: 'create:form_submissions', type: 'C', description: 'Save draft/submit form submission values' },
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
        { key: 'delete:reports', type: 'D', description: 'Delete report executions' },
    ],
    '': [


        // ── correspondence ──────────────────────────────────────────────────────
        // { key: 'read:notifications', description: 'Access notification admin endpoints' },
        // { key: 'update:notifications', description: 'Mark or update own notification state' },
        // { key: 'send:email', description: 'Send email via the correspondence provider' },
        // { key: 'read:subscriptions', description: 'List own or resource correspondence subscriptions' },
        // { key: 'create:subscriptions', description: 'Follow a resource (create correspondence subscription)' },
        // { key: 'update:subscriptions', description: 'Update subscription channel preferences' },
        // { key: 'delete:subscriptions', description: 'Unfollow a resource (deactivate subscription)' },

        // // ── cron ────────────────────────────────────────────────────────────────
        // { key: 'read:cron', description: 'View cron job definitions and status' },
        // { key: 'update:cron', description: 'Trigger or update cron job definitions' },

        // // ── help portal ───────────────────────────────────────────────────────────
        // { key: 'read:help_portal', description: 'View in-app help catalog and articles' },



        // // ── reports ─────────────────────────────────────────────────────────────

        // { key: 'approve:reports', description: 'Approve reports via workflow tasks' },



        // // ── token-vault ─────────────────────────────────────────────────────────
        // { key: 'read:oauth_token', description: 'View OAuth token records' },
        // { key: 'create:oauth_token', description: 'Create or refresh OAuth tokens' },
        // { key: 'delete:oauth_token', description: 'Revoke OAuth tokens' },





        // // ── comment entity-type permissions (consumer-defined) ──────────────────
        // { key: 'read:donation_comments', description: 'Read comments on donation entities' },
        // { key: 'create:donation_comments', description: 'Post comments on donation entities' },
        // { key: 'read:task_comments', description: 'Read comments on task entities' },
        // { key: 'create:task_comments', description: 'Post comments on task entities' },





        // // ── expenses (consumer-defined) ──────────────────────────────────────────
        // { key: 'create:expense', description: 'Create expense record' },
        // { key: 'update:expense', description: 'Update expense record' },
        // { key: 'finalize:expense', description: 'Finalize (approve) expense' },
        // { key: 'settle:expense', description: 'Settle (pay) expense' },
        // { key: 'read:expenses', description: 'View expense records' },

        // // ── earnings (consumer-defined) ──────────────────────────────────────────
        // { key: 'create:earning', description: 'Create earning record' },
        // { key: 'update:earning', description: 'Update earning record' },
        // { key: 'read:earnings', description: 'View earning records' },

        // // ── project (consumer-defined) ───────────────────────────────────────────
        // { key: 'read:projects', description: 'View project records' },
        // { key: 'create:project', description: 'Create projects' },
        // { key: 'update:project', description: 'Update projects' },
        // { key: 'read:activities', description: 'View project activities' },
        // { key: 'create:activity', description: 'Create project activities' },
        // { key: 'update:activity', description: 'Update project activities' },
        // { key: 'read:beneficiaries', description: 'View project beneficiaries' },
        // { key: 'create:beneficiary', description: 'Create project beneficiaries' },
        // { key: 'update:beneficiary', description: 'Update project beneficiaries' },
        // { key: 'read:goals', description: 'View project goals' },
        // { key: 'create:goal', description: 'Create project goals' },
        // { key: 'update:goal', description: 'Update project goals' },
        // { key: 'read:milestones', description: 'View project milestones' },
        // { key: 'create:milestone', description: 'Create project milestones' },
        // { key: 'update:milestone', description: 'Update project milestones' },
        // { key: 'read:project_teams', description: 'View project team members' },
        // { key: 'create:project_team', description: 'Add project team members' },
        // { key: 'update:project_team', description: 'Update project team members' },
        // { key: 'read:risks', description: 'View project risks' },
        // { key: 'create:risk', description: 'Create project risks' },
        // { key: 'update:risk', description: 'Update project risks' },

        // // ── requests (consumer-defined) ───────────────────────────────

        // { key: 'read:tasks', description: 'View assigned workflow tasks (inbox)' },
        // { key: 'update:task', description: 'Claim, complete, or delegate workflow tasks' },
        // { key: 'admin:workflows', description: 'Administrative workflow operations (force-skip, stuck detector)' },
        // { key: 'manage:workflow_definitions', description: 'Publish and manage workflow definitions' },

        // // ── meeting (consumer-defined) ───────────────────────────────────────────
        // { key: 'read:meetings', description: 'View meeting records' },
        // { key: 'create:meeting', description: 'Schedule meetings and sync with Google Calendar' },
        // { key: 'update:meeting', description: 'Update or cancel meeting details' },
        // { key: 'delete:meeting', description: 'Delete meeting records' },

        // // ── asset (consumer-defined) ─────────────────────────────────────────────
        // { key: 'read:assets', description: 'View physical asset records' },
        // { key: 'create:asset', description: 'Register physical assets' },
        // { key: 'update:asset', description: 'Update assets and assign or return custody' },
        // { key: 'delete:asset', description: 'Soft-delete physical asset records' },

        // // ── book bank (library under assets hub) ─────────────────────────────────
        // { key: 'read:books', description: 'View book bank records' },
        // { key: 'create:book', description: 'Register books in the book bank' },
        // { key: 'update:book', description: 'Update books and apply lend/return/donate operations' },
        // { key: 'delete:book', description: 'Soft-delete book bank records' },

        // // ── public site ───────────────────────────────────────────────────────────
        // { key: 'read:public_content', description: 'Read public site content' },
    ]
};