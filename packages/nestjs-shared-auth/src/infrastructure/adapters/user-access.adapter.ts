import { Inject, Injectable, Logger, Optional } from '@nestjs/common';
import { ICACHE_PORT, ICachePort, IUserLookupPort, UserInfo } from '@nabarun-ngo/nestjs-shared-core';
import { IUserAccessPort } from '../../application/ports/user-access.port';
import { AuthUser, ScopedRbacContext } from '../../application/models/auth-user';
import { AUTH_OPTIONS } from '../auth-options.token';
import { AuthModuleOptions } from '../../auth-options';
import { IUserRoleRepository } from '../../domain/repositories/user-role.repository';
import { IUserRoleGroupRepository } from '../../domain/repositories/user-role-group.repository';
import { IUserPermissionRepository } from '../../domain/repositories/user-permission.repository';

@Injectable()
export class UserAccessAdapter implements IUserAccessPort {
  private readonly logger = new Logger(UserAccessAdapter.name);
  constructor(
    @Inject(IUserRoleRepository) private readonly userRoleRepo: IUserRoleRepository,
    @Inject(IUserRoleGroupRepository) private readonly userRoleGroupRepo: IUserRoleGroupRepository,
    @Inject(IUserPermissionRepository)
    private readonly userPermissionRepo: IUserPermissionRepository,
    @Inject(AUTH_OPTIONS) private readonly options: AuthModuleOptions,
    @Inject(ICACHE_PORT) private readonly cache: ICachePort,
    @Optional() @Inject(IUserLookupPort) private readonly userLookup: IUserLookupPort | null,
  ) { }

  private cacheKey(idpSub: string): string {
    return `user-access:${idpSub}`;
  }

  async resolve(idpSub: string): Promise<AuthUser> {
    const ttl = this.options.cache?.userAccessTtlMs ?? 1_800_000;
    return this.cache.getOrSet(this.cacheKey(idpSub), () => this.resolveFromDb(idpSub), ttl);
  }

  async invalidate(idpSub: string): Promise<void> {
    await this.cache.del(this.cacheKey(idpSub));
  }

  async invalidateMany(idpSubs: string[]): Promise<void> {
    const unique = [...new Set(idpSubs.filter(Boolean))];
    await Promise.all(unique.map((idpSub) => this.invalidate(idpSub)));
  }

  private async resolveFromDb(idpSub: string): Promise<AuthUser> {
    const [directRoles, groupMemberships, directPermissions] = await Promise.all([
      this.userRoleRepo.resolveDirectPermissions(idpSub),
      this.userRoleGroupRepo.resolveGroupPermissions(idpSub),
      this.userPermissionRepo.resolveDirectUserPermissions(idpSub),
    ]);

    if (!this.userLookup) {
      this.logger.warn('UserLookupPort not found, user details may not be available');
    }

    let userInfo: UserInfo | null = null;
    if (this.userLookup) {
      userInfo = await this.userLookup.findByIdPSub(idpSub);
    }

    console.log(userInfo);

    const permissionSet = new Set<string>();
    const roleSet = new Set<string>();
    const groupSet = new Set<string>();
    const scopedRole: Record<string, ScopedRbacContext> = {};
    const key = (entityId: string, entityType: string) => `${entityType}:${entityId}`;
    for (const view of directRoles) {
      if (view.entityId && view.entityType) {
        const roleKey = key(view.entityId, view.entityType);
        scopedRole[roleKey].entityId = view.entityId;
        scopedRole[roleKey].entityType = view.entityType;
        scopedRole[roleKey].permissions.push(...view.permissionKeys);
        scopedRole[roleKey].userRoles.push(view.roleKey);
      } else {
        roleSet.add(view.roleKey);
        view.permissionKeys.forEach((k) => permissionSet.add(k));
      }
    }

    for (const view of groupMemberships) {
      if (view.entityId && view.entityType) {
        const roleKey = key(view.entityId, view.entityType);
        scopedRole[roleKey].entityId = view.entityId;
        scopedRole[roleKey].entityType = view.entityType;
        if (!scopedRole[roleKey].roleGroups.includes(view.groupKey)) {
          scopedRole[roleKey].roleGroups.push(view.groupKey);
        }
        view.roleKeys.forEach((k) => {
          if (!scopedRole[roleKey].userRoles.includes(k)) scopedRole[roleKey].userRoles.push(k);
        });
        view.permissionKeys.forEach((k) => {
          if (!scopedRole[roleKey].permissions.includes(k)) scopedRole[roleKey].permissions.push(k);
        });
      } else {
        groupSet.add(view.groupKey);
        view.roleKeys.forEach((k) => roleSet.add(k));
        view.permissionKeys.forEach((k) => permissionSet.add(k));
      }
    }

    for (const view of directPermissions) {
      if (view.entityId && view.entityType) {
        const roleKey = key(view.entityId, view.entityType);
        scopedRole[roleKey].entityId = view.entityId;
        scopedRole[roleKey].entityType = view.entityType;
        if (!scopedRole[roleKey].permissions.includes(view.permissionKey)) {
          scopedRole[roleKey].permissions.push(view.permissionKey);
        }
      } else {
        permissionSet.add(view.permissionKey);
      }
    }

    return {
      type: 'jwt',
      idpSub,
      userId: userInfo?.id ?? undefined,
      userInfo: userInfo ?? undefined,
      roleGroups: [...groupSet],
      permissions: [...permissionSet],
      userRoles: [...roleSet],
      scopedAccess: Object.values(scopedRole),
    };
  }
}
