import { Injectable } from '@nestjs/common';
import {
  ICorrespondenceEventResolver,
  CorrespondenceEventResolver,
  NotificationSpec,
} from '@nabarun-ngo/nestjs-shared-correspondence';
import { UserCreatedEvent } from '../../domain/events/user-created.event';
import { EmailTemplateKey } from '../../../../shared/enums/email-template-key';

/**
 * Welcome email on every successful user create.
 * Includes title, the Auth0 password-change ticket URL, and login instructions.
 * Does not include any generated password.
 */
@Injectable()
@CorrespondenceEventResolver()
export class UserCreatedCorrespondenceResolver
  implements ICorrespondenceEventResolver<UserCreatedEvent> {
  readonly eventType = UserCreatedEvent;

  resolve(event: UserCreatedEvent): NotificationSpec[] | null {
    return [
      {
        recipients: { mode: 'users', userIds: [event.userId] },
        channels: {
          email: {
            templateKey: EmailTemplateKey.UserWelcome,
            templateData: {
              email: event.email,
              title: event.title ?? '',
              setPasswordUrl: event.setPasswordUrl ?? '',
            },
          },
        },
      },
    ];
  }
}
