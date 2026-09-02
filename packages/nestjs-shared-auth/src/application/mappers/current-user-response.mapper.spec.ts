import { mapAuthUserToResponse } from './current-user-response.mapper';
import { AuthUser } from '../models/auth-user';

function jwtUser(overrides: Partial<AuthUser> = {}): AuthUser {
  return {
    type: 'jwt',
    idpSub: 'auth0|abc',
    userId: 'u-1',
    email: 'asha.verma@example.org',
    permissions: ['read:projects'],
    userRoles: ['editor'],
    roleGroups: ['field_team'],
    idpClaims: { sub: 'auth0|abc' },
    userInfo: {
      id: 'u-1',
      firstName: 'Asha',
      lastName: 'Verma',
      fullName: 'Asha Verma',
      phoneNo: '+919876543210',
      attributes: new Map([['profileComplete', true]]),
    },
    ...overrides,
  };
}

describe('mapAuthUserToResponse', () => {
  it('flattens profile fields and converts attributes Map including profileComplete', () => {
    expect(mapAuthUserToResponse(jwtUser())).toEqual({
      type: 'jwt',
      idpSub: 'auth0|abc',
      id: 'u-1',
      firstName: 'Asha',
      lastName: 'Verma',
      fullName: 'Asha Verma',
      email: 'asha.verma@example.org',
      permissions: ['read:projects'],
      userRoles: ['editor'],
      roleGroups: ['field_team'],
      idpClaims: { sub: 'auth0|abc' },
      attributes: { profileComplete: true },
      phoneNo: '+919876543210',
      scopedAccess: [],
    });
  });

  it('keeps profileComplete in JSON so GET /auth/me does not drop the Map', () => {
    const rawMapJson = JSON.stringify({
      attributes: new Map([['profileComplete', true]]),
    });
    expect(JSON.parse(rawMapJson).attributes).toEqual({});

    const body = JSON.parse(JSON.stringify(mapAuthUserToResponse(jwtUser())));
    expect(body.attributes).toEqual({ profileComplete: true });
  });

  it('emits profileComplete false when the profile is incomplete', () => {
    const result = mapAuthUserToResponse(
      jwtUser({
        userInfo: {
          id: 'u-1',
          attributes: new Map([['profileComplete', false]]),
        },
      }),
    );

    expect(result.attributes).toEqual({ profileComplete: false });
  });

  it('passes through attributes that are already a plain object', () => {
    const result = mapAuthUserToResponse(
      jwtUser({
        userInfo: {
          id: 'u-1',
          attributes: { profileComplete: true } as unknown as Map<string, unknown>,
        },
      }),
    );

    expect(result.attributes).toEqual({ profileComplete: true });
  });

  it('maps scopedAccess and uses empty collections when optional fields are missing', () => {
    const authUser: AuthUser = {
      type: 'apikey',
      idpSub: 'apikey:key-1',
      permissions: [],
      userRoles: [],
      roleGroups: [],
      scopedAccess: [
        {
          entityId: 'proj-A',
          entityType: 'project',
          permissions: ['update:project'],
          userRoles: ['coordinator'],
          roleGroups: ['field_team'],
        },
      ],
    };

    const result = mapAuthUserToResponse(authUser);

    expect(result.id).toBeUndefined();
    expect(result.attributes).toEqual({});
    expect(result.scopedAccess).toEqual([
      {
        entityId: 'proj-A',
        entityType: 'project',
        permissions: ['update:project'],
        userRoles: ['coordinator'],
        roleGroups: ['field_team'],
      },
    ]);
  });
});
