import { Injectable } from '@nestjs/common';
import {
  BasePrismaService,
  PrismaCrudRepositoryBase,
} from '@nabarun-ngo/nestjs-shared-persistence';
import { PrismaClient } from '../../prisma/client';
import {
  AuthUserPermissionWhereInput,
  AuthUserPermissionWhereUniqueInput,
  AuthUserPermissionCreateInput,
  AuthUserPermissionUncheckedCreateInput,
  AuthUserPermissionUpdateInput,
  AuthUserPermissionUncheckedUpdateInput,
  AuthUserPermissionOrderByWithRelationInput,
} from '../../prisma/models/AuthUserPermission';
import {
  UserPermission,
  UserPermissionFilter,
} from '@nabarun-ngo/nestjs-shared-auth/domain/aggregates/user-permission/user-permission.aggregate';
import {
  DirectUserPermissionView,
  IUserPermissionRepository,
} from '@nabarun-ngo/nestjs-shared-auth/domain/repositories/user-permission.repository';

type UserPermissionRow = {
  id: string;
  idpSub: string;
  ownerId: string | null;
  entityId: string | null;
  entityType: string | null;
  permissionId: string;
  grantedAt: Date;
  revokedAt: Date | null;
  grantedBy: string | null;
  revokedBy: string | null;
  note: string | null;
  permission?: { key: string; deletedAt: Date | null };
};

@Injectable()
export class UserPermissionPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'authUserPermission',
    UserPermission,
    string,
    UserPermissionFilter,
    UserPermissionRow,
    AuthUserPermissionWhereInput,
    AuthUserPermissionWhereUniqueInput,
    ({} & AuthUserPermissionUncheckedCreateInput) | ({} & AuthUserPermissionCreateInput),
    ({} & AuthUserPermissionUncheckedUpdateInput) | ({} & AuthUserPermissionUpdateInput),
    AuthUserPermissionOrderByWithRelationInput
  >
  implements IUserPermissionRepository
{
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'authUserPermission');
  }

  protected toDomain(row: UserPermissionRow): UserPermission {
    return new UserPermission({
      id: row.id,
      idpSub: row.idpSub,
      ownerId: row.ownerId ?? undefined,
      entityId: row.entityId ?? undefined,
      entityType: row.entityType ?? undefined,
      permissionId: row.permissionId,
      permissionKey: row.permission?.key,
      grantedAt: row.grantedAt,
      revokedAt: row.revokedAt ?? undefined,
      grantedBy: row.grantedBy ?? undefined,
      revokedBy: row.revokedBy ?? undefined,
      note: row.note ?? undefined,
    });
  }

  protected toCreateInput(
    entity: UserPermission,
  ): ({} & AuthUserPermissionUncheckedCreateInput) | ({} & AuthUserPermissionCreateInput) {
    return {
      id: entity.id,
      idpSub: entity.idpSub,
      ownerId: entity.ownerId ?? null,
      entityId: entity.entityId ?? null,
      entityType: entity.entityType ?? null,
      permissionId: entity.permissionId,
      grantedAt: entity.grantedAt,
      grantedBy: entity.grantedBy ?? null,
      note: entity.note ?? null,
    };
  }

  protected toUpdateInput(
    _id: string,
    entity: UserPermission,
  ): ({} & AuthUserPermissionUncheckedUpdateInput) | ({} & AuthUserPermissionUpdateInput) {
    return {
      revokedAt: entity.revokedAt ?? null,
      revokedBy: entity.revokedBy ?? null,
      note: entity.note ?? null,
    };
  }

  protected toUniqueWhere(id: string): AuthUserPermissionWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(filter?: UserPermissionFilter): AuthUserPermissionWhereInput {
    return {
      ...(filter?.idpSub ? { idpSub: filter.idpSub } : {}),
      ...(filter?.permissionId ? { permissionId: filter.permissionId } : {}),
      ...(filter?.ownerId ? { ownerId: filter.ownerId } : {}),
      ...(filter?.entityId ? { entityId: filter.entityId } : {}),
      ...(filter?.entityType ? { entityType: filter.entityType } : {}),
      ...(filter?.isActive === true ? { revokedAt: null } : {}),
    };
  }

  protected defaultOrderBy(): AuthUserPermissionOrderByWithRelationInput {
    return { grantedAt: 'desc' };
  }

  protected supportsSoftDelete(): boolean {
    return false;
  }

  protected toInclude() {
    return { permission: true } as any;
  }

  async findActiveByIdPSub(idpSub: string): Promise<UserPermission[]> {
    const rows = await this.delegate.findMany({
      where: { idpSub, revokedAt: null },
      include: { permission: true },
    });
    return (rows as UserPermissionRow[]).map((r) => this.toDomain(r));
  }

  async resolveDirectUserPermissions(idpSub: string): Promise<DirectUserPermissionView[]> {
    const rows = await this.delegate.findMany({
      where: { idpSub, revokedAt: null },
      include: { permission: true },
    });
    return (rows as UserPermissionRow[])
      .filter((r) => r.permission && !r.permission.deletedAt)
      .map((r) => ({
        permissionKey: r.permission!.key,
        ownerId: r.ownerId ?? undefined,
        entityId: r.entityId ?? undefined,
        entityType: r.entityType ?? undefined,
      }));
  }
}
