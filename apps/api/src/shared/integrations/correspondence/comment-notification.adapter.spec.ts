import { CorrespondenceFacade, NotificationSpec } from '@nabarun-ngo/nestjs-shared-correspondence';
import { CommentNotificationAdapter } from './comment-notification.adapter';
import { EmailTemplateKey } from '../../email-template-key';

describe('CommentNotificationAdapter', () => {
  let dispatch: jest.Mock<Promise<void>, [NotificationSpec]>;
  let adapter: CommentNotificationAdapter;

  beforeEach(() => {
    dispatch = jest.fn().mockResolvedValue(undefined);
    adapter = new CommentNotificationAdapter({ dispatch } as unknown as CorrespondenceFacade);
  });

  it('dispatches a user-targeted spec for a mention', async () => {
    await adapter.notifyMention({
      commentId: 'c1',
      entityType: 'task',
      entityId: 't1',
      authorName: 'Alice',
      mentionUserId: 'u2',
      mentionEmail: 'u2@example.com',
      mentionDisplayName: 'Bob',
    });

    expect(dispatch).toHaveBeenCalledTimes(1);
    const spec = dispatch.mock.calls[0][0];
    expect(spec.recipients).toEqual({ mode: 'users', userIds: ['u2'] });
    expect(spec.channels.email?.templateKey).toBe(EmailTemplateKey.CommentMention);
    expect(spec.channels.email?.overrideEmails).toEqual(['u2@example.com']);
  });

  it('dispatches a resource-mode spec (with excludes) for a comment added', async () => {
    await adapter.notifyCommentAdded({
      commentId: 'c1',
      entityType: 'task',
      entityId: 't1',
      authorName: 'Alice',
      excludeUserIds: ['u1', 'u2'],
    });

    expect(dispatch).toHaveBeenCalledTimes(1);
    const spec = dispatch.mock.calls[0][0];
    expect(spec.recipients).toEqual({
      mode: 'resource',
      referenceType: 'task',
      referenceId: 't1',
      excludeUserIds: ['u1', 'u2'],
    });
    expect(spec.channels.email?.templateKey).toBe(EmailTemplateKey.CommentAdded);
  });
});
