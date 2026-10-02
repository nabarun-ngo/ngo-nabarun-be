import { InitiatePasswordChangeHandler } from './initiate-password-change.handler';
import { InitiatePasswordChangeCommand } from './initiate-password-change.command';
import { IUserRepository } from '../../../domain/repositories/user.repository';
import { IIdentityProvider } from '../../../domain/ports/identity-provider.port';
import { User } from '../../../domain/aggregates/user/user.aggregate';
import { UserStatus } from '../../../domain/enums/user-status.enum';
import {
  UserNotFoundError,
  IdentityNotLinkedError,
  InvalidCredentialsError,
} from '../../../domain/errors/user.errors';
import type { UserModuleOptions } from '../../../user.schema';

function makeUser(idpSub?: string): User {
  return User.rehydrate({
    id: 'user-id-1',
    email: 'john@example.com',
    status: UserStatus.ACTIVE,
    firstName: 'John',
    lastName: 'Doe',
    isProfileComplete: false,
    isPublic: true,
    socialMediaLinks: [],
    deletedAt: null,
    version: 0,
    idpSub,
  });
}

describe('InitiatePasswordChangeHandler', () => {
  let repo: jest.Mocked<Pick<IUserRepository, 'findById'>>;
  let identityProvider: jest.Mocked<
    Pick<IIdentityProvider, 'verifyPassword' | 'createPasswordChangeTicket'>
  >;
  let handler: InitiatePasswordChangeHandler;

  const options = {
    appFeUrl: 'https://app.example.com',
  } as UserModuleOptions;

  beforeEach(() => {
    repo = { findById: jest.fn().mockResolvedValue(makeUser('auth0|abc')) };
    identityProvider = {
      verifyPassword: jest.fn().mockResolvedValue(undefined),
      createPasswordChangeTicket: jest.fn().mockResolvedValue({
        ticketUrl: 'https://auth0.example/lo/reset?ticket=t1',
      }),
    };
    handler = new InitiatePasswordChangeHandler(
      repo as unknown as IUserRepository,
      identityProvider as unknown as IIdentityProvider,
      options,
    );
  });

  function cmd(
    overrides: Partial<{ userId: string; currentPassword: string; redirectUrl: string }> = {},
  ) {
    return new InitiatePasswordChangeCommand({
      userId: overrides.userId ?? 'user-id-1',
      requestorId: 'user-id-1',
      currentPassword: overrides.currentPassword ?? 'CurrentP@ss1',
      redirectUrl: overrides.redirectUrl,
    });
  }

  it('verifies password then returns a short-lived ticket (email not marked verified)', async () => {
    const result = await handler.execute(cmd());

    expect(identityProvider.verifyPassword).toHaveBeenCalledWith(
      'john@example.com',
      'CurrentP@ss1',
    );
    expect(identityProvider.createPasswordChangeTicket).toHaveBeenCalledWith({
      userId: 'auth0|abc',
      markEmailAsVerified: false,
      includeEmailInRedirect: false,
      ttlSec: 600,
      resultUrl: 'https://app.example.com',
    });
    expect(result.ticketUrl).toBe('https://auth0.example/lo/reset?ticket=t1');
  });

  it('honours a client redirect URL within the configured app origin', async () => {
    await handler.execute(cmd({ redirectUrl: 'https://app.example.com/dashboard' }));

    expect(identityProvider.createPasswordChangeTicket).toHaveBeenCalledWith(
      expect.objectContaining({ resultUrl: 'https://app.example.com/dashboard' }),
    );
  });

  it('falls back to the default app URL when redirect origin differs', async () => {
    await handler.execute(cmd({ redirectUrl: 'https://evil.example.net/steal' }));

    expect(identityProvider.createPasswordChangeTicket).toHaveBeenCalledWith(
      expect.objectContaining({ resultUrl: 'https://app.example.com' }),
    );
  });

  it('falls back to the default app URL when redirect URL is malformed', async () => {
    await handler.execute(cmd({ redirectUrl: 'not-a-url' }));

    expect(identityProvider.createPasswordChangeTicket).toHaveBeenCalledWith(
      expect.objectContaining({ resultUrl: 'https://app.example.com' }),
    );
  });

  it('throws UserNotFoundError when user does not exist', async () => {
    repo.findById.mockResolvedValue(null);
    await expect(handler.execute(cmd({ userId: 'ghost' }))).rejects.toThrow(UserNotFoundError);
    expect(identityProvider.verifyPassword).not.toHaveBeenCalled();
  });

  it('throws IdentityNotLinkedError when idpSub is absent', async () => {
    repo.findById.mockResolvedValue(makeUser(undefined));
    await expect(handler.execute(cmd())).rejects.toThrow(IdentityNotLinkedError);
    expect(identityProvider.verifyPassword).not.toHaveBeenCalled();
  });

  it('propagates InvalidCredentialsError when password verification fails', async () => {
    identityProvider.verifyPassword.mockRejectedValue(new InvalidCredentialsError());
    await expect(handler.execute(cmd())).rejects.toThrow(InvalidCredentialsError);
    expect(identityProvider.createPasswordChangeTicket).not.toHaveBeenCalled();
  });
});
