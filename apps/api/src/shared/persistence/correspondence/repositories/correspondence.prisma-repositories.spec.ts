import {
  Notification,
  NotificationPriority,
  NotificationType,
} from '@nabarun-ngo/nestjs-shared-correspondence/domain/aggregates/notification.aggregate';
import { UserNotification } from '@nabarun-ngo/nestjs-shared-correspondence/domain/aggregates/user-notification.aggregate';
import {
  ResourceSubscription,
  SubscriberType,
  SubscribedVia,
} from '@nabarun-ngo/nestjs-shared-correspondence/domain/aggregates/resource-subscription.aggregate';
import { NotificationPrismaRepository } from './notification.prisma-repository';
import { ResourceSubscriptionPrismaRepository } from './resource-subscription.prisma-repository';
import { UserNotificationPrismaRepository } from './user-notification.prisma-repository';

const NOW = new Date('2026-08-23T08:00:00.000Z');

const notificationRow = {
  id: 'notification-1',
  title: 'Title',
  body: 'Body',
  type: NotificationType.INFO,
  category: 'general',
  priority: NotificationPriority.NORMAL,
  actionUrl: null,
  actionType: null,
  actionData: null,
  referenceId: null,
  referenceType: null,
  dispatchId: null,
  imageUrl: null,
  icon: null,
  metadata: null,
  expiresAt: null,
  createdAt: NOW,
  updatedAt: NOW,
};

const userNotificationRow = {
  id: 'user-notification-1',
  notificationId: 'notification-1',
  userId: 'user-1',
  isRead: false,
  readAt: null,
  isArchived: false,
  archivedAt: null,
  isPushSent: false,
  pushSentAt: null,
  pushDelivered: false,
  pushError: null,
  createdAt: NOW,
  updatedAt: NOW,
};

const subscriptionRow = {
  id: 'subscription-1',
  subscriberType: SubscriberType.USER,
  userId: 'user-1',
  userEmail: 'user@example.com',
  userName: 'User',
  roleName: null,
  resourceType: 'PROJECT',
  resourceId: 'project-1',
  subscribedVia: SubscribedVia.MANUAL,
  isActive: true,
  createdAt: NOW,
  updatedAt: NOW,
  channels: [],
};

function delegate() {
  return {
    create: jest.fn(),
    update: jest.fn(),
    delete: jest.fn(),
    deleteMany: jest.fn(),
    updateMany: jest.fn(),
    findUnique: jest.fn(),
    findFirst: jest.fn(),
    findMany: jest.fn(),
    count: jest.fn(),
    groupBy: jest.fn(),
    upsert: jest.fn(),
  };
}

function makeRepositories() {
  const client = {
    correspondenceNotification: delegate(),
    correspondenceUserNotification: delegate(),
    correspondenceResourceSubscription: delegate(),
    correspondenceSubscriptionChannel: delegate(),
  };
  const database = { client };
  return {
    client,
    notification: new NotificationPrismaRepository(database as any),
    userNotification: new UserNotificationPrismaRepository(database as any),
    subscription: new ResourceSubscriptionPrismaRepository(database as any),
  };
}

describe('correspondence Prisma repository delegate alignment', () => {
  it('routes notification retention to correspondenceNotification.deleteMany', async () => {
    const { client, notification } = makeRepositories();
    const cutoff = new Date('2026-01-01T00:00:00.000Z');
    client.correspondenceNotification.deleteMany.mockResolvedValue({ count: 3 });

    await expect(notification.deleteExpiredBefore(cutoff)).resolves.toBe(3);
    expect(client.correspondenceNotification.deleteMany).toHaveBeenCalledWith({
      where: { expiresAt: { not: null, lt: cutoff } },
    });
  });

  it('uses canonical notification delegates for CRUD, paging, and push bulk updates', async () => {
    const { client, notification } = makeRepositories();
    const entity = new Notification(
      notificationRow.id,
      notificationRow.title,
      notificationRow.body,
      NotificationType.INFO,
      notificationRow.category,
      { createdAt: NOW, updatedAt: NOW },
    );
    client.correspondenceNotification.create.mockResolvedValue(notificationRow);
    client.correspondenceNotification.findMany.mockResolvedValue([]);
    client.correspondenceNotification.count.mockResolvedValue(0);
    client.correspondenceUserNotification.updateMany.mockResolvedValue({ count: 2 });

    await expect(notification.create(entity)).resolves.toBeInstanceOf(Notification);
    await notification.findPaged();
    await notification.bulkMarkPushSent(['recipient-1', 'recipient-2'], true);

    expect(client.correspondenceNotification.create).toHaveBeenCalledWith({
      data: expect.objectContaining({ id: notificationRow.id }),
    });
    expect(client.correspondenceNotification.findMany).toHaveBeenCalledWith(
      expect.objectContaining({ skip: 0, take: 50 }),
    );
    expect(client.correspondenceUserNotification.updateMany).toHaveBeenCalledWith(
      expect.objectContaining({ where: { id: { in: ['recipient-1', 'recipient-2'] } } }),
    );
  });

  it('uses correspondenceUserNotification for representative CRUD and bulk paths', async () => {
    const { client, userNotification } = makeRepositories();
    const entity = new UserNotification(
      userNotificationRow.id,
      userNotificationRow.notificationId,
      userNotificationRow.userId,
      { createdAt: NOW, updatedAt: NOW },
    );
    client.correspondenceUserNotification.create.mockResolvedValue(userNotificationRow);
    client.correspondenceUserNotification.updateMany.mockResolvedValue({ count: 1 });

    await expect(userNotification.create(entity)).resolves.toBeInstanceOf(UserNotification);
    await userNotification.markAllReadForUser('user-1');

    expect(client.correspondenceUserNotification.create).toHaveBeenCalledWith({
      data: expect.objectContaining({
        id: userNotificationRow.id,
        notificationId: userNotificationRow.notificationId,
      }),
    });
    expect(client.correspondenceUserNotification.updateMany).toHaveBeenCalledWith(
      expect.objectContaining({ where: { userId: 'user-1', isRead: false } }),
    );
  });

  it('routes subscription retention to correspondenceResourceSubscription.deleteMany', async () => {
    const { client, subscription } = makeRepositories();
    const cutoff = new Date('2026-01-01T00:00:00.000Z');
    client.correspondenceResourceSubscription.deleteMany.mockResolvedValue({ count: 4 });

    await expect(subscription.deleteInactiveBefore(cutoff)).resolves.toBe(4);
    expect(client.correspondenceResourceSubscription.deleteMany).toHaveBeenCalledWith({
      where: { isActive: false, updatedAt: { lt: cutoff } },
    });
  });

  it('uses canonical subscription and channel delegates for create and update', async () => {
    const { client, subscription } = makeRepositories();
    const entity = new ResourceSubscription(
      subscriptionRow.id,
      SubscriberType.USER,
      subscriptionRow.resourceType,
      SubscribedVia.MANUAL,
      {
        userId: subscriptionRow.userId,
        userEmail: subscriptionRow.userEmail,
        resourceId: subscriptionRow.resourceId,
        createdAt: NOW,
        updatedAt: NOW,
      },
    );
    client.correspondenceResourceSubscription.create.mockResolvedValue(subscriptionRow);
    client.correspondenceResourceSubscription.update.mockResolvedValue(subscriptionRow);
    client.correspondenceResourceSubscription.findUnique.mockResolvedValue(subscriptionRow);

    await expect(subscription.create(entity)).resolves.toBeInstanceOf(ResourceSubscription);
    await expect(subscription.update(entity.id, entity)).resolves.toBeInstanceOf(ResourceSubscription);

    expect(client.correspondenceResourceSubscription.create).toHaveBeenCalled();
    expect(client.correspondenceResourceSubscription.update).toHaveBeenCalledWith(
      expect.objectContaining({ where: { id: subscriptionRow.id } }),
    );
    expect(client.correspondenceResourceSubscription.findUnique).toHaveBeenCalledWith({
      where: { id: subscriptionRow.id },
      include: { channels: true },
    });
    expect(client.correspondenceSubscriptionChannel.upsert).not.toHaveBeenCalled();
  });
});
