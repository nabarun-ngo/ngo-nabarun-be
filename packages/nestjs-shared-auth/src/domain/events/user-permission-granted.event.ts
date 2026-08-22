import { DomainEvent } from '@nabarun-ngo/nestjs-shared-core';
import type { UserPermission } from '../aggregates/user-permission/user-permission.aggregate';

export type UserPermissionGrantedSnapshot = Pick<
  UserPermission,
  'id' | 'idpSub' | 'permissionId' | 'ownerId'
>;

export class UserPermissionGrantedEvent extends DomainEvent<UserPermissionGrantedSnapshot> {
  constructor(snapshot: UserPermissionGrantedSnapshot) {
    super(snapshot.id, snapshot);
  }
}
