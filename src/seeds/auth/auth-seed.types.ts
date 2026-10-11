export interface AuthPermissionSeed {
  key: string;
  description?: string;
}

export interface AuthRoleSeed {
  key: string;
  description?: string;
  /** When true, role is platform/break-glass and hidden from member pickers by default. */
  isShadow?: boolean;
  permissionKeys: string[];
  seedUsers?: string[];
}

export interface AuthRoleGroupSeed {
  key: string;
  description?: string;
  /** When true, group is platform/break-glass and hidden from member pickers by default. */
  isShadow?: boolean;
  roleKeys: string[];
  seedUsers?: string[];
}

export interface AuthSeedData {
  permissions: AuthPermissionSeed[];
  roles: AuthRoleSeed[];
  roleGroups?: AuthRoleGroupSeed[];
}
