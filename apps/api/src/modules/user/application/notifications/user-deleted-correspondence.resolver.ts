import { Injectable } from '@nestjs/common';
import {
  ICorrespondenceEventResolver,
  CorrespondenceEventResolver,
  NotificationSpec,
} from '@nabarun-ngo/nestjs-shared-correspondence';
import { UserDeletedEvent } from '../../domain/events/user-deleted.event';
import { EmailTemplateKey } from '../../../../shared/email-template-key';

/**
 * Pure resolver: sends a deactivation email. `overrideEmails` bypasses
 * IUserLookupPort resolution because the user is soft-deleted at this point.
 */
@Injectable()
@CorrespondenceEventResolver()
export class UserDeletedCorrespondenceResolver
  implements ICorrespondenceEventResolver<UserDeletedEvent>
{
  readonly eventType = UserDeletedEvent;

  resolve(event: UserDeletedEvent): NotificationSpec[] | null {
    return [
      {
        recipients: { mode: 'users', userIds: [event.userId] },
        channels: {
          email: {
            templateKey: EmailTemplateKey.UserDeactivated,
            templateData: { email: event.email },
            overrideEmails: [event.email],
          },
        },
      },
    ];
  }
}
