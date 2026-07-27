import { IUserAccessPort } from '@nabarun-ngo/nestjs-shared-auth';
import { OnUserDeletedHandler } from './on-user-deleted.handler';
import { UserDeletedEvent } from '../../../../domain/events/user-deleted.event';

describe('OnUserDeletedHandler', () => {
  let userAccess: jest.Mocked<Pick<IUserAccessPort, 'invalidate'>>;
  let handler: OnUserDeletedHandler;

  beforeEach(() => {
    userAccess = { invalidate: jest.fn().mockResolvedValue(undefined) };
    handler = new OnUserDeletedHandler(userAccess as unknown as IUserAccessPort);
  });

  it('invalidates Auth cache when idpSub is present', async () => {
    const event = new UserDeletedEvent('user-1', 'john@example.com', 'auth0|abc');
    await handler.handle(event);
    expect(userAccess.invalidate).toHaveBeenCalledWith('auth0|abc');
  });

  it('skips cache invalidation when idpSub is absent', async () => {
    const event = new UserDeletedEvent('user-1', 'john@example.com', undefined);
    await handler.handle(event);
    expect(userAccess.invalidate).not.toHaveBeenCalled();
  });
});
