import { Injectable } from '@nestjs/common';
import {
  CommentAddedNotification,
  CommentMentionNotification,
  ICommentNotificationPort,
} from '@nabarun-ngo/nestjs-shared-comment';
import {
  CorrespondenceFacade,
  NotificationSpec,
  NotificationType,
} from '@nabarun-ngo/nestjs-shared-correspondence';
import { EmailTemplateKey } from '../../email-template-key';

/**
 * Host adapter implementing the comment context's outbound notification port. Owns
 * the mapping from comment events to correspondence notifications (wording, template
 * keys, channels) and dispatches pre-built specs via the CorrespondenceFacade.
 */
@Injectable()
export class CommentNotificationAdapter implements ICommentNotificationPort {
  constructor(private readonly correspondence: CorrespondenceFacade) {}

  async notifyMention(d: CommentMentionNotification): Promise<void> {
    const spec: NotificationSpec = {
      recipients: { mode: 'users', userIds: [d.mentionUserId] },
      channels: {
        inApp: {
          title: `${d.authorName} mentioned you in a comment`,
          body: `You were mentioned in a comment on ${d.entityType} ${d.entityId}.`,
          type: NotificationType.INFO,
          category: 'WORKFLOW',
          referenceId: d.commentId,
          referenceType: 'comment',
        },
        email: {
          templateKey: EmailTemplateKey.CommentMention,
          overrideEmails: [d.mentionEmail],
          templateData: {
            authorName: d.authorName,
            displayName: d.mentionDisplayName,
            entityType: d.entityType,
            entityId: d.entityId,
            commentId: d.commentId,
          },
        },
        push: { enabled: true },
      },
    };
    await this.correspondence.dispatch(spec);
  }

  async notifyCommentAdded(d: CommentAddedNotification): Promise<void> {
    const spec: NotificationSpec = {
      recipients: {
        mode: 'resource',
        referenceType: d.entityType,
        referenceId: d.entityId,
        excludeUserIds: d.excludeUserIds,
      },
      channels: {
        inApp: {
          title: 'New comment added',
          body: `${d.authorName} added a comment.`,
          type: NotificationType.INFO,
          category: 'WORKFLOW',
          referenceId: d.commentId,
          referenceType: 'comment',
        },
        email: {
          templateKey: EmailTemplateKey.CommentAdded,
          templateData: {
            authorName: d.authorName,
            entityType: d.entityType,
            entityId: d.entityId,
            commentId: d.commentId,
          },
        },
        push: { enabled: true },
      },
    };
    await this.correspondence.dispatch(spec);
  }
}
