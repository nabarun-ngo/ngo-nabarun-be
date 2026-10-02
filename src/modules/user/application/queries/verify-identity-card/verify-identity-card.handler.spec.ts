import { VerifyIdentityCardHandler } from './verify-identity-card.handler';
import { VerifyIdentityCardQuery } from './verify-identity-card.query';
import { IdentityCardVerificationOutcome } from '../../dtos/identity-card.dto';
import { User } from '../../../domain/aggregates/user/user.aggregate';
import { UserStatus } from '../../../domain/enums/user-status.enum';
import { UniqueMemberId } from '../../../domain/value-objects/unique-member-id.vo';
import { IUserRepository } from '../../../domain/repositories/user.repository';

const validId = UniqueMemberId.compose('2412', 1);

function makeUser(status: UserStatus): User {
  return User.rehydrate({
    id: 'user-1',
    email: 'asha@example.com',
    status,
    firstName: 'Asha',
    lastName: 'Verma',
    isProfileComplete: true,
    isPublic: true,
    socialMediaLinks: [],
    uniqueMemberId: validId,
  });
}

describe('VerifyIdentityCardHandler', () => {
  let users: jest.Mocked<Pick<IUserRepository, 'findByUniqueMemberId'>>;
  let handler: VerifyIdentityCardHandler;

  beforeEach(() => {
    users = { findByUniqueMemberId: jest.fn() };
    handler = new VerifyIdentityCardHandler(users as unknown as IUserRepository);
  });

  it('returns VALID for an active member', async () => {
    users.findByUniqueMemberId.mockResolvedValue(makeUser(UserStatus.ACTIVE));
    const result = await handler.execute(new VerifyIdentityCardQuery(validId));
    expect(result.outcome).toBe(IdentityCardVerificationOutcome.VALID);
    expect(result.displayName).toBe('Asha Verma');
  });

  it('returns INVALID for a blocked member', async () => {
    users.findByUniqueMemberId.mockResolvedValue(makeUser(UserStatus.BLOCKED));
    const result = await handler.execute(new VerifyIdentityCardQuery(validId));
    expect(result.outcome).toBe(IdentityCardVerificationOutcome.INVALID);
  });

  it('returns INVALID for a deleted member', async () => {
    users.findByUniqueMemberId.mockResolvedValue(makeUser(UserStatus.DELETED));
    const result = await handler.execute(new VerifyIdentityCardQuery(validId));
    expect(result.outcome).toBe(IdentityCardVerificationOutcome.INVALID);
  });

  it('returns UNKNOWN for a malformed id', async () => {
    const result = await handler.execute(new VerifyIdentityCardQuery('not-an-id'));
    expect(result.outcome).toBe(IdentityCardVerificationOutcome.UNKNOWN);
    expect(users.findByUniqueMemberId).not.toHaveBeenCalled();
  });

  it('returns UNKNOWN when no member has that number', async () => {
    users.findByUniqueMemberId.mockResolvedValue(null);
    const result = await handler.execute(new VerifyIdentityCardQuery(validId));
    expect(result.outcome).toBe(IdentityCardVerificationOutcome.UNKNOWN);
  });
});
