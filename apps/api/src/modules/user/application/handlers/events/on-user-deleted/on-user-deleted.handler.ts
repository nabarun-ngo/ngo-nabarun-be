import { Inject, Injectable, Logger } from '@nestjs/common';
import { EventsHandler, IEventHandler } from '@nestjs/cqrs';
import { IUserAccessPort } from '@nabarun-ngo/nestjs-shared-auth';
import { UserDeletedEvent } from '../../../../domain/events/user-deleted.event';

/**
 * Handles UserDeletedEvent:
 * - Invalidates Auth's user-access:{sub} cache so the soft-deleted user's
 *   cached profile is evicted immediately.
 *
 * The deactivation-email notification is produced separately by
 * UserDeletedCorrespondenceResolver (event-driven correspondence).
 */
@Injectable()
@EventsHandler(UserDeletedEvent)
export class OnUserDeletedHandler implements IEventHandler<UserDeletedEvent> {
  private readonly logger = new Logger(OnUserDeletedHandler.name);

  constructor(
    @Inject(IUserAccessPort) private readonly userAccess: IUserAccessPort,
  ) { }

  async handle(event: UserDeletedEvent): Promise<void> {
    this.logger.log(`User deleted: ${event.userId} (${event.email})`);

    if (event.idpSub) {
      await this.userAccess.invalidate(event.idpSub);
    }

    // TODO: cleanup DMS profile documents if Dms2 is wired

    // TODO: delete UserCustomField records for event.userId when the custom-fields module is implemented
  }
}
