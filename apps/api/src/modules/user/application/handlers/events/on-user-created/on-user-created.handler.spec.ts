import { AuthFacade } from '@nabarun-ngo/nestjs-shared-auth';
import { OnUserCreatedHandler } from './on-user-created.handler';
import { UserCreatedEvent } from '../../../../domain/events/user-created.event';
import type { UserModuleOptions } from '../../../../user.schema';

const defaultOptions: UserModuleOptions = {
  idp: {
    domain: 'test.auth0.com',
    clientId: 'client-id',
    clientSecret: 'client-secret',
    connections: {
      default: { name: 'Username-Password-Authentication', type: 'password', provisionOnCreate: true },
    },
  },
  defaultRoleKeys: [],
  passwordExpiresInDays: 90,
};

describe('OnUserCreatedHandler', () => {
  let authFacade: jest.Mocked<Pick<AuthFacade, 'grantRole'>>;
  let handler: OnUserCreatedHandler;

  function buildHandler(options: UserModuleOptions = defaultOptions): OnUserCreatedHandler {
    return new OnUserCreatedHandler(options, authFacade as unknown as AuthFacade);
  }

  beforeEach(() => {
    authFacade = { grantRole: jest.fn().mockResolvedValue({ roleId: 'role-1' }) };
    handler = buildHandler();
  });

  describe('default role grant', () => {
    it('grants each configured default role via AuthFacade', async () => {
      handler = buildHandler({ ...defaultOptions, defaultRoleKeys: ['MEMBER', 'VOLUNTEER'] });
      const event = new UserCreatedEvent('user-1', 'a@b.com', 'auth0|sub', false);

      await handler.handle(event);

      expect(authFacade.grantRole).toHaveBeenCalledTimes(2);
      expect(authFacade.grantRole).toHaveBeenCalledWith('auth0|sub', 'MEMBER', 'user-1');
      expect(authFacade.grantRole).toHaveBeenCalledWith('auth0|sub', 'VOLUNTEER', 'user-1');
    });

    it('skips role grant when defaultRoleKeys is empty', async () => {
      const event = new UserCreatedEvent('user-1', 'a@b.com', 'auth0|sub', false);
      await handler.handle(event);
      expect(authFacade.grantRole).not.toHaveBeenCalled();
    });

    it('skips role grant when idpSub is absent', async () => {
      handler = buildHandler({ ...defaultOptions, defaultRoleKeys: ['MEMBER'] });
      const event = new UserCreatedEvent('user-1', 'a@b.com', undefined, false);
      await handler.handle(event);
      expect(authFacade.grantRole).not.toHaveBeenCalled();
    });

    it('continues granting remaining roles if one role grant fails', async () => {
      handler = buildHandler({ ...defaultOptions, defaultRoleKeys: ['MEMBER', 'VOLUNTEER'] });
      authFacade.grantRole
        .mockRejectedValueOnce(new Error('role not found'))
        .mockResolvedValueOnce({ roleId: 'role-2' } as any);

      const event = new UserCreatedEvent('user-1', 'a@b.com', 'auth0|sub', false);
      await expect(handler.handle(event)).resolves.not.toThrow();
      expect(authFacade.grantRole).toHaveBeenCalledTimes(2);
    });
  });
});
