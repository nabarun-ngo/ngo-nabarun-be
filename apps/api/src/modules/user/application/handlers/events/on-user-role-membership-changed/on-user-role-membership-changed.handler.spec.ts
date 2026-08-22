import { AuthFacade } from '@nabarun-ngo/nestjs-shared-auth';
import { UserRoleGrantedEvent } from '@nabarun-ngo/nestjs-shared-auth/domain/events/user-role-granted.event';
import { UserRoleRevokedEvent } from '@nabarun-ngo/nestjs-shared-auth/domain/events/user-role-revoked.event';
import { OnUserRoleMembershipChangedHandler } from './on-user-role-membership-changed.handler';
import type { IUserRepository } from '../../../../domain/repositories/user.repository';

describe('OnUserRoleMembershipChangedHandler', () => {
  let authFacade: jest.Mocked<Pick<AuthFacade, 'getUserRoles'>>;
  let users: jest.Mocked<Pick<IUserRepository, 'updateRoleKeysByIdPSub'>>;
  let handler: OnUserRoleMembershipChangedHandler;

  beforeEach(() => {
    authFacade = {
      getUserRoles: jest.fn().mockResolvedValue([
        { roleKey: 'MEMBER' },
        { roleKey: 'VOLUNTEER' },
        { roleKey: 'MEMBER' },
        { roleKey: undefined },
      ]),
    };
    users = {
      updateRoleKeysByIdPSub: jest.fn().mockResolvedValue(undefined),
    };
    handler = new OnUserRoleMembershipChangedHandler(
      authFacade as unknown as AuthFacade,
      users as unknown as IUserRepository,
    );
  });

  it('loads roles via AuthFacade and persists deduped roleKeys', async () => {
    const event = new UserRoleGrantedEvent({
      id: 'ur-1',
      idpSub: 'auth0|sub',
      roleId: 'role-1',
      ownerId: undefined,
    });

    await handler.handle(event);

    expect(authFacade.getUserRoles).toHaveBeenCalledWith('auth0|sub');
    expect(users.updateRoleKeysByIdPSub).toHaveBeenCalledWith('auth0|sub', [
      'MEMBER',
      'VOLUNTEER',
    ]);
  });

  it('clears / updates keys on revoke when Auth returns remaining roles', async () => {
    authFacade.getUserRoles.mockResolvedValueOnce([{ roleKey: 'MEMBER' }]);
    const event = new UserRoleRevokedEvent({
      id: 'ur-1',
      idpSub: 'auth0|sub',
      roleId: 'role-2',
      ownerId: undefined,
    });

    await handler.handle(event);

    expect(users.updateRoleKeysByIdPSub).toHaveBeenCalledWith('auth0|sub', [
      'MEMBER',
    ]);
  });

  it('no-ops when idpSub is missing', async () => {
    const event = new UserRoleGrantedEvent({
      id: 'ur-1',
      idpSub: '',
      roleId: 'role-1',
      ownerId: undefined,
    });

    await handler.handle(event);

    expect(authFacade.getUserRoles).not.toHaveBeenCalled();
    expect(users.updateRoleKeysByIdPSub).not.toHaveBeenCalled();
  });

  it('swallows errors from AuthFacade / repository', async () => {
    authFacade.getUserRoles.mockRejectedValueOnce(new Error('auth down'));
    const event = new UserRoleGrantedEvent({
      id: 'ur-1',
      idpSub: 'auth0|sub',
      roleId: 'role-1',
      ownerId: undefined,
    });

    await expect(handler.handle(event)).resolves.not.toThrow();
    expect(users.updateRoleKeysByIdPSub).not.toHaveBeenCalled();
  });
});
