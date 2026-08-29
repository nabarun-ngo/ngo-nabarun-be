import { Injectable } from '@nestjs/common';
import {
  BasePrismaService,
  PrismaCrudRepositoryBase,
} from '@nabarun-ngo/nestjs-shared-persistence';
import { Notification, NotificationFilter, NotificationType, NotificationPriority } from '@nabarun-ngo/nestjs-shared-correspondence/domain/aggregates/notification.aggregate';
import { UserNotification } from '@nabarun-ngo/nestjs-shared-correspondence/domain/aggregates/user-notification.aggregate';
import { INotificationRepository } from '@nabarun-ngo/nestjs-shared-correspondence/domain/repositories/notification.repository';
import type { PrismaClient } from '../../prisma/client';
import type {
  CorrespondenceNotificationModel,
  CorrespondenceNotificationWhereInput,
  CorrespondenceNotificationWhereUniqueInput,
  CorrespondenceNotificationCreateInput,
  CorrespondenceNotificationUncheckedCreateInput,
  CorrespondenceNotificationUpdateInput,
  CorrespondenceNotificationUncheckedUpdateInput,
  CorrespondenceNotificationOrderByWithRelationInput,
} from '../../prisma/models/CorrespondenceNotification';

@Injectable()
export class NotificationPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'correspondenceNotification',
    Notification,
    string,
    NotificationFilter,
    CorrespondenceNotificationModel,
    CorrespondenceNotificationWhereInput,
    CorrespondenceNotificationWhereUniqueInput,
    ({} & CorrespondenceNotificationCreateInput) | ({} & CorrespondenceNotificationUncheckedCreateInput),
    ({} & CorrespondenceNotificationUpdateInput) | ({} & CorrespondenceNotificationUncheckedUpdateInput),
    CorrespondenceNotificationOrderByWithRelationInput
  >
  implements INotificationRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'correspondenceNotification');
  }

  async createWithUserNotifications(
    notification: Notification,
    userNotifications: UserNotification[],
  ): Promise<Notification> {
    const row = await this.delegate.create({
      data: {
        ...this.toCreateInput(notification),
        userNotifications: {
          create: userNotifications.map((un) => ({
            id: un.id,
            userId: un.userId,
            isRead: false,
            isArchived: false,
            isPushSent: false,
            pushDelivered: false,
            createdAt: un.createdAt,
            updatedAt: un.updatedAt,
          })),
        },
      },
    });
    return this.toDomain(row);
  }

  async bulkMarkPushSent(
    userNotificationIds: string[],
    success: boolean,
    error?: string,
  ): Promise<void> {
    await this.client.correspondenceUserNotification.updateMany({
      where: { id: { in: userNotificationIds } },
      data: {
        isPushSent: true,
        pushSentAt: new Date(),
        pushDelivered: success,
        pushError: error ?? null,
        updatedAt: new Date(),
      },
    });
  }

  async deleteExpiredBefore(date: Date): Promise<number> {
    const result = await this.delegate.deleteMany({
      where: { expiresAt: { not: null, lt: date } },
    });
    return result.count;
  }

  async getDeliveryStatuses(
    notificationIds: string[],
  ): Promise<Map<string, 'failed' | 'succeeded'>> {
    const result = new Map<string, 'failed' | 'succeeded'>();
    if (notificationIds.length === 0) return result;

    const [failed, delivered] = await Promise.all([
      this.client.correspondenceUserNotification.groupBy({
        by: ['notificationId'],
        where: {
          notificationId: { in: notificationIds },
          isPushSent: true,
          pushDelivered: false,
        },
      }),
      this.client.correspondenceUserNotification.groupBy({
        by: ['notificationId'],
        where: { notificationId: { in: notificationIds }, pushDelivered: true },
      }),
    ]);

    // "succeeded" first, then let any failure override — failed takes precedence.
    for (const row of delivered as { notificationId: string }[]) {
      result.set(row.notificationId, 'succeeded');
    }
    for (const row of failed as { notificationId: string }[]) {
      result.set(row.notificationId, 'failed');
    }
    return result;
  }

  protected toUniqueWhere(id: string): CorrespondenceNotificationWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(filter?: NotificationFilter): CorrespondenceNotificationWhereInput {
    if (!filter) return {};
    return {
      ...(filter.type ? { type: filter.type } : {}),
      ...(filter.category ? { category: filter.category } : {}),
      ...(filter.priority ? { priority: filter.priority } : {}),
      ...(filter.referenceId ? { referenceId: filter.referenceId } : {}),
      ...(filter.referenceType ? { referenceType: filter.referenceType } : {}),
      ...(filter.dispatchId ? { dispatchId: filter.dispatchId } : {}),
      ...(filter.status === 'failed'
        ? { userNotifications: { some: { isPushSent: true, pushDelivered: false } } }
        : {}),
      ...(filter.status === 'succeeded'
        ? { userNotifications: { some: { pushDelivered: true } } }
        : {}),
      ...(filter.fromDate || filter.toDate
        ? {
          createdAt: {
            ...(filter.fromDate ? { gte: filter.fromDate } : {}),
            ...(filter.toDate ? { lte: filter.toDate } : {}),
          },
        }
        : {}),
    };
  }

  protected toCreateInput(
    notification: Notification,
  ): ({} & CorrespondenceNotificationCreateInput) | ({} & CorrespondenceNotificationUncheckedCreateInput) {
    return {
      id: notification.id,
      title: notification.title,
      body: notification.body,
      type: notification.type,
      category: notification.category,
      priority: notification.priority,
      actionUrl: notification.action?.url ?? null,
      actionType: notification.action?.type ?? null,
      actionData: notification.action?.data ?? null,
      referenceId: notification.referenceId ?? null,
      referenceType: notification.referenceType ?? null,
      dispatchId: notification.dispatchId ?? null,
      imageUrl: notification.imageUrl ?? null,
      icon: notification.icon ?? null,
      metadata: (notification.metadata) ?? null,
      expiresAt: notification.expiresAt ?? null,
      createdAt: notification.createdAt,
      updatedAt: notification.updatedAt,
    } as unknown as
      | ({} & CorrespondenceNotificationCreateInput)
      | ({} & CorrespondenceNotificationUncheckedCreateInput);
  }

  protected toUpdateInput(
    _id: string,
    notification: Notification,
  ): ({} & CorrespondenceNotificationUpdateInput) | ({} & CorrespondenceNotificationUncheckedUpdateInput) {
    return {
      title: notification.title,
      body: notification.body,
      type: notification.type,
      category: notification.category,
      priority: notification.priority,
      updatedAt: notification.updatedAt,
    };
  }

  protected defaultOrderBy(): CorrespondenceNotificationOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected defaultPageSize(): number {
    return 50;
  }

  protected toDomain(row: CorrespondenceNotificationModel): Notification {
    return new Notification(row.id, row.title, row.body, row.type as NotificationType, row.category as string, {
      priority: row.priority as NotificationPriority,
      action:
        row.actionUrl || row.actionType || row.actionData
          ? {
            url: row.actionUrl ?? undefined,
            type: row.actionType ?? undefined,
            data: row.actionData as Record<string, any> | undefined,
          }
          : undefined,
      referenceId: row.referenceId ?? undefined,
      referenceType: row.referenceType ?? undefined,
      dispatchId: row.dispatchId ?? undefined,
      imageUrl: row.imageUrl ?? undefined,
      icon: row.icon ?? undefined,
      metadata: row.metadata as Record<string, any> | undefined,
      expiresAt: row.expiresAt ?? undefined,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    });
  }
}
