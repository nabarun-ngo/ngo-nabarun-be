import { UserDeletedCorrespondenceResolver } from './user-deleted-correspondence.resolver';
import { UserDeletedEvent } from '../../domain/events/user-deleted.event';
import { EmailTemplateKey } from '../../../../shared/enums/email-template-key';

describe('UserDeletedCorrespondenceResolver', () => {
  const resolver = new UserDeletedCorrespondenceResolver();

  it('targets UserDeletedEvent', () => {
    expect(resolver.eventType).toBe(UserDeletedEvent);
  });

  it('builds a deactivation email spec with overrideEmails', () => {
    const specs = resolver.resolve(
      new UserDeletedEvent('user-1', 'john@example.com', 'auth0|abc'),
    );
    expect(specs).toEqual([
      {
        recipients: { mode: 'users', userIds: ['user-1'] },
        channels: {
          email: {
            templateKey: EmailTemplateKey.UserDeactivated,
            templateData: { email: 'john@example.com' },
            overrideEmails: ['john@example.com'],
          },
        },
      },
    ]);
  });
});
