import { IdentityCardIssuePolicy } from './identity-card-issue.policy';
import { User } from '../aggregates/user/user.aggregate';
import { UserStatus } from '../enums/user-status.enum';
import { IdentityCardNotIssuableError } from '../errors/user.errors';

function makeUser(overrides: Partial<{ status: UserStatus; complete: boolean }> = {}): User {
  return User.rehydrate({
    id: 'u1',
    email: 'a@b.com',
    status: overrides.status ?? UserStatus.ACTIVE,
    firstName: 'Asha',
    lastName: 'Verma',
    isProfileComplete: overrides.complete ?? true,
    isPublic: true,
    socialMediaLinks: [],
    uniqueMemberId: 'NM24120011',
  });
}

describe('IdentityCardIssuePolicy', () => {
  it('allows an active complete member', () => {
    expect(() => IdentityCardIssuePolicy.assertCanPrint(makeUser())).not.toThrow();
  });

  it('rejects blocked members', () => {
    expect(() =>
      IdentityCardIssuePolicy.assertCanPrint(makeUser({ status: UserStatus.BLOCKED })),
    ).toThrow(IdentityCardNotIssuableError);
  });

  it('rejects incomplete profiles', () => {
    expect(() =>
      IdentityCardIssuePolicy.assertCanPrint(makeUser({ complete: false })),
    ).toThrow(IdentityCardNotIssuableError);
  });
});
