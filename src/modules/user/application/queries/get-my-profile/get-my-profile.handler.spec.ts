import { GetMyProfileHandler } from './get-my-profile.handler';
import { GetMyProfileQuery } from './get-my-profile.query';
import { IUserRepository } from '../../../domain/repositories/user.repository';
import { User } from '../../../domain/aggregates/user/user.aggregate';
import { UserStatus } from '../../../domain/enums/user-status.enum';
import { UserNotFoundError } from '../../../domain/errors/user.errors';

function makeUser(isProfileComplete = true): User {
  return User.rehydrate({
    id: 'user-id-1',
    email: 'john@example.com',
    status: UserStatus.ACTIVE,
    firstName: 'John',
    lastName: 'Doe',
    isProfileComplete,
    isPublic: true,
    socialMediaLinks: [],
    deletedAt: null,
    version: 0,
    idpSub: 'auth0|abc',
    dateOfBirth: isProfileComplete ? new Date('1990-01-01') : undefined,
    gender: isProfileComplete ? 'MALE' : undefined,
  });
}

describe('GetMyProfileHandler', () => {
  let repo: jest.Mocked<Pick<IUserRepository, 'findById' | 'findByIdPSub'>>;
  let handler: GetMyProfileHandler;

  beforeEach(() => {
    repo = {
      findById: jest.fn().mockResolvedValue(makeUser()),
      findByIdPSub: jest.fn().mockResolvedValue(makeUser()),
    };
    handler = new GetMyProfileHandler(repo as unknown as IUserRepository);
  });

  it('returns the user DTO when found by userId', async () => {
    const result = await handler.execute(
      new GetMyProfileQuery({ userId: 'user-id-1' }),
    );

    expect(result.email).toBe('john@example.com');
    expect(result.isProfileComplete).toBe(true);
    expect(repo.findById).toHaveBeenCalledWith('user-id-1');
    expect(repo.findByIdPSub).not.toHaveBeenCalled();
  });

  it('falls back to idpSub when userId lookup misses', async () => {
    repo.findById.mockResolvedValue(null);

    const result = await handler.execute(
      new GetMyProfileQuery({ userId: 'missing-id', idpSub: 'auth0|abc' }),
    );

    expect(result.email).toBe('john@example.com');
    expect(repo.findById).toHaveBeenCalledWith('missing-id');
    expect(repo.findByIdPSub).toHaveBeenCalledWith('auth0|abc');
  });

  it('returns the user DTO when found by idpSub only', async () => {
    const result = await handler.execute(
      new GetMyProfileQuery({ idpSub: 'auth0|abc' }),
    );

    expect(result.email).toBe('john@example.com');
    expect(repo.findById).not.toHaveBeenCalled();
    expect(repo.findByIdPSub).toHaveBeenCalledWith('auth0|abc');
  });

  it('includes missingFields in the response', async () => {
    repo.findById.mockResolvedValue(makeUser(false));
    const result = await handler.execute(
      new GetMyProfileQuery({ userId: 'user-id-1' }),
    );

    expect(result.missingFields).toBeDefined();
    expect(Array.isArray(result.missingFields)).toBe(true);
  });

  it('missingFields is empty when profile is complete', async () => {
    const result = await handler.execute(
      new GetMyProfileQuery({ userId: 'user-id-1' }),
    );
    expect(result.missingFields).toEqual([]);
  });

  it('missingFields lists absent required fields', async () => {
    repo.findById.mockResolvedValue(makeUser(false));
    const result = await handler.execute(
      new GetMyProfileQuery({ userId: 'user-id-1' }),
    );

    expect(result.missingFields).toEqual(
      expect.arrayContaining(['dateOfBirth', 'gender']),
    );
  });

  it('throws UserNotFoundError when no user is found', async () => {
    repo.findById.mockResolvedValue(null);
    repo.findByIdPSub.mockResolvedValue(null);

    await expect(
      handler.execute(new GetMyProfileQuery({ userId: 'ghost', idpSub: 'auth0|ghost' })),
    ).rejects.toThrow(UserNotFoundError);
  });
});
