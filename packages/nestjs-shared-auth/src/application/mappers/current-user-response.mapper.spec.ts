import { mapAuthUserToResponse } from './current-user-response.mapper';
import { AuthUser } from '../models/auth-user';

describe('mapAuthUserToResponse', () => {
  it('flattens profile fields and converts attributes Map including profileComplete', () => {
    const authUser: AuthUser = {
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
    };

    expect(mapAuthUserToResponse(authUser)).toEqual({
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
