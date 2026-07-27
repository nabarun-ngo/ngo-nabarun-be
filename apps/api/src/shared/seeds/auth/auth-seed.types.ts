export interface AuthPermissionSeed {
  key: string;
  description?: string;
}

export interface AuthRoleSeed {
  key: string;
  description?: string;
  permissionKeys: string[];
  seedUsers?: string[];
}

export interface AuthRoleGroupSeed {
  key: string;
  description?: string;
  roleKeys: string[];
  seedUsers?: string[];
}

export interface AuthSeedData {
  permissions: AuthPermissionSeed[];
  roles: AuthRoleSeed[];
  roleGroups?: AuthRoleGroupSeed[];
}
