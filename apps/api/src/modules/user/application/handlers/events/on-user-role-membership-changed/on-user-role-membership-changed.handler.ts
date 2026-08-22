import { Inject, Injectable, Logger } from '@nestjs/common';
import { EventsHandler, IEventHandler } from '@nestjs/cqrs';
import { AuthFacade } from '@nabarun-ngo/nestjs-shared-auth';
import { UserRoleGrantedEvent } from '@nabarun-ngo/nestjs-shared-auth/domain/events/user-role-granted.event';
import { UserRoleRevokedEvent } from '@nabarun-ngo/nestjs-shared-auth/domain/events/user-role-revoked.event';
import { UserRoleGroupGrantedEvent } from '@nabarun-ngo/nestjs-shared-auth/domain/events/user-role-group-granted.event';
import { UserRoleGroupRevokedEvent } from '@nabarun-ngo/nestjs-shared-auth/domain/events/user-role-group-revoked.event';
import { IUserRepository } from '../../../../domain/repositories/user.repository';

type RoleMembershipChangedEvent =
  | UserRoleGrantedEvent
  | UserRoleRevokedEvent
  | UserRoleGroupGrantedEvent
  | UserRoleGroupRevokedEvent;

/**
 * Keeps UserProfile.roleKeys in sync when Auth role or role-group membership changes.
 * Auth remains the source of truth; this is a denormalized projection for member payloads.
 *
 * Failures are logged and swallowed so Auth cache-invalidation handlers are not blocked.
 */
@Injectable()
@EventsHandler(
  UserRoleGrantedEvent,
  UserRoleRevokedEvent,
  UserRoleGroupGrantedEvent,
  UserRoleGroupRevokedEvent,
)
export class OnUserRoleMembershipChangedHandler
  implements IEventHandler<RoleMembershipChangedEvent>
{
  private readonly logger = new Logger(OnUserRoleMembershipChangedHandler.name);

  constructor(
    private readonly authFacade: AuthFacade,
    @Inject(IUserRepository) private readonly users: IUserRepository,
  ) {}

  async handle(event: RoleMembershipChangedEvent): Promise<void> {
    const idpSub = event.snapshot?.idpSub;
    if (!idpSub?.trim()) {
      return;
    }

    try {
      const memberships = await this.authFacade.getUserRoles(idpSub);
      const roleKeys = [
        ...new Set(
          memberships
            .map((m) => m.roleKey)
            .filter((key): key is string => !!key?.trim()),
        ),
      ];
      await this.users.updateRoleKeysByIdPSub(idpSub, roleKeys);
      this.logger.debug(
        `Synced roleKeys for ${idpSub}: [${roleKeys.join(', ')}]`,
      );
    } catch (err) {
      this.logger.error(
        `Failed to sync roleKeys for ${idpSub}: ${err instanceof Error ? err.message : err}`,
      );
    }
  }
}
