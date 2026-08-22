import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';
import { RequestRecord } from '../../domain/models/request.model';

export function actorRoles(user: AuthUser): string[] {
  return user.userRoles ?? [];
}

export function actorGroups(user: AuthUser): string[] {
  return user.roleGroups ?? [];
}

export function actorPermissions(user: AuthUser): string[] {
  return user.permissions ?? [];
}

export function hasAnyRole(userRoles: string[], required: string[]): boolean {
  if (!required.length) return false;
  const set = new Set(userRoles.map((r) => r.toUpperCase()));
  return required.some((r) => set.has(r.toUpperCase()));
}

export function hasAnyGroup(userGroups: string[], required: string[]): boolean {
  if (!required.length) return false;
  const set = new Set(userGroups.map((g) => g.toUpperCase()));
  return required.some((g) => set.has(g.toUpperCase()));
}

export function hasAnyPermission(
  userPermissions: string[],
  required: string[],
): boolean {
  if (!required.length) return false;
  const set = new Set(userPermissions);
  return required.some((permission) => set.has(permission));
}

/** Role ∪ group ∪ permission eligibility; empty lists are no-ops for that dimension. */
export function isEligible(
  userRoles: string[],
  userGroups: string[],
  userPermissions: string[],
  roles: string[],
  groups: string[],
  permissions: string[],
): boolean {
  return (
    hasAnyRole(userRoles, roles)
    || hasAnyGroup(userGroups, groups)
    || hasAnyPermission(userPermissions, permissions)
  );
}

export function canFulfill(
  request: Pick<
    RequestRecord,
    'executorRoles' | 'executorGroups' | 'executorPermissions'
  >,
  user: AuthUser,
): boolean {
  return isEligible(
    actorRoles(user),
    actorGroups(user),
    actorPermissions(user),
    request.executorRoles ?? [],
    request.executorGroups ?? [],
    request.executorPermissions ?? [],
  );
}

export function canApprove(
  request: Pick<
    RequestRecord,
    'approverRoles' | 'approverGroups' | 'approverPermissions'
  >,
  user: AuthUser,
): boolean {
  return isEligible(
    actorRoles(user),
    actorGroups(user),
    actorPermissions(user),
    request.approverRoles ?? [],
    request.approverGroups ?? [],
    request.approverPermissions ?? [],
  );
}

export function hasPermission(user: AuthUser, permission: string): boolean {
  return (user.permissions ?? []).includes(permission);
}
