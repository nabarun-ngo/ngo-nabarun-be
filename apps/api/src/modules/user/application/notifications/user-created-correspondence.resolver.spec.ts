import { UserCreatedCorrespondenceResolver } from './user-created-correspondence.resolver';
import { UserCreatedEvent } from '../../domain/events/user-created.event';
import { EmailTemplateKey } from '../../../../shared/email-template-key';

describe('UserCreatedCorrespondenceResolver', () => {
  const resolver = new UserCreatedCorrespondenceResolver();

  it('targets UserCreatedEvent', () => {
    expect(resolver.eventType).toBe(UserCreatedEvent);
  });

  it('builds a welcome email spec when the password was system-generated', () => {
    const specs = resolver.resolve(
      new UserCreatedEvent('user-1', 'a@b.com', 'auth0|sub', true),
    );
    expect(specs).toEqual([
      {
        recipients: { mode: 'users', userIds: ['user-1'] },
        channels: {
          email: {
            templateKey: EmailTemplateKey.UserWelcome,
            templateData: { email: 'a@b.com' },
          },
        },
      },
    ]);
  });

  it('returns null when the password was not system-generated', () => {
    const specs = resolver.resolve(
      new UserCreatedEvent('user-1', 'a@b.com', 'auth0|sub', false),
    );
    expect(specs).toBeNull();
  });
});
