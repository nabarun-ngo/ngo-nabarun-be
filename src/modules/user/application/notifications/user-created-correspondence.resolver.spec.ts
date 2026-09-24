import { UserCreatedCorrespondenceResolver } from './user-created-correspondence.resolver';
import { UserCreatedEvent } from '../../domain/events/user-created.event';
import { EmailTemplateKey } from '../../../../shared/enums/email-template-key';

describe('UserCreatedCorrespondenceResolver', () => {
  const resolver = new UserCreatedCorrespondenceResolver();

  it('targets UserCreatedEvent', () => {
    expect(resolver.eventType).toBe(UserCreatedEvent);
  });

  it('always builds a welcome email spec with title and set-password URL', () => {
    const specs = resolver.resolve(
      new UserCreatedEvent(
        'user-1',
        'a@b.com',
        'auth0|sub',
        'Ms',
        'https://tenant.auth0.com/lo/reset?ticket=abc',
      ),
    );
    expect(specs).toEqual([
      {
        recipients: { mode: 'users', userIds: ['user-1'] },
        channels: {
          email: {
            templateKey: EmailTemplateKey.UserWelcome,
            templateData: {
              email: 'a@b.com',
              title: 'Ms',
              setPasswordUrl: 'https://tenant.auth0.com/lo/reset?ticket=abc',
            },
          },
        },
      },
    ]);
  });
});
