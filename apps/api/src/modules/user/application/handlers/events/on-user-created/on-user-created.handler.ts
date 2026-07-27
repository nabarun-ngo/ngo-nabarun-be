import { Inject, Injectable, Logger } from '@nestjs/common';
import { EventsHandler, IEventHandler } from '@nestjs/cqrs';
import { AuthFacade } from '@nabarun-ngo/nestjs-shared-auth';
import { UserCreatedEvent } from '../../../../domain/events/user-created.event';
import { USER_OPTIONS } from '../../../../infrastructure/user-options.token';
import type { UserModuleOptions } from '../../../../user.schema';

/**
 * Handles UserCreatedEvent:
 * - Grants each key in `defaultRoleKeys` via AuthFacade when the user has an idpSub.
 *   Role grant failures are logged but never propagate — the user record already exists.
 *
 * The welcome-email notification is produced separately by
 * UserCreatedCorrespondenceResolver (event-driven correspondence).
 *
 * AuthFacade is provided by AuthModule. UserModule must be imported AFTER AuthModule
 * in the consuming app for this injection to resolve.
 */
@Injectable()
@EventsHandler(UserCreatedEvent)
export class OnUserCreatedHandler implements IEventHandler<UserCreatedEvent> {
  private readonly logger = new Logger(OnUserCreatedHandler.name);

  constructor(
    @Inject(USER_OPTIONS) private readonly options: UserModuleOptions,
    private readonly authFacade: AuthFacade,
  ) { }

  async handle(event: UserCreatedEvent): Promise<void> {
    this.logger.log(`User created: ${event.userId} (${event.email})`);

    if (this.options.defaultRoleKeys.length > 0 && event.idpSub) {
      for (const roleKey of this.options.defaultRoleKeys) {
        try {
          await this.authFacade.grantRole(event.idpSub, roleKey, event.userId);
          this.logger.debug(`Granted role '${roleKey}' to ${event.idpSub}`);
        } catch (err) {
          this.logger.error(
            `Failed to grant default role '${roleKey}' to ${event.idpSub}: ${err instanceof Error ? err.message : err}`,
          );
        }
      }
    }
  }
}
