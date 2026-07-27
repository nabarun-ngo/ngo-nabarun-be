import { Injectable } from '@nestjs/common';
import {
  ICorrespondenceEventResolver,
  CorrespondenceEventResolver,
  NotificationSpec,
} from '@nabarun-ngo/nestjs-shared-correspondence';
import { UserCreatedEvent } from '../../domain/events/user-created.event';
import { EmailTemplateKey } from '../../../../shared/enums/email-template-key';

/**
 * Pure resolver: sends a welcome email only for the system-generated-password
 * flow. The event already carries everything needed — no repository lookup.
 */
@Injectable()
@CorrespondenceEventResolver()
export class UserCreatedCorrespondenceResolver
  implements ICorrespondenceEventResolver<UserCreatedEvent> {
  readonly eventType = UserCreatedEvent;

  resolve(event: UserCreatedEvent): NotificationSpec[] | null {
    if (!event.systemGeneratedPassword) return null;
    return [
      {
        recipients: { mode: 'users', userIds: [event.userId] },
        channels: {
          email: {
            templateKey: EmailTemplateKey.UserWelcome,
            templateData: { email: event.email },
          },
        },
      },
    ];
  }
}
