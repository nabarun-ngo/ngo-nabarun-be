import { Injectable } from '@nestjs/common';
import {
  BasePrismaService,
  PrismaCrudRepositoryBase,
} from '@nabarun-ngo/nestjs-shared-persistence';
import { UserNotification, UserNotificationFilter } from '@nabarun-ngo/nestjs-shared-correspondence/domain/aggregates/user-notification.aggregate';
import { IUserNotificationRepository } from '@nabarun-ngo/nestjs-shared-correspondence/domain/repositories/user-notification.repository';
import type { PrismaClient } from '../../prisma/client';
import type {
  CorrespondenceUserNotificationModel,
  CorrespondenceUserNotificationWhereInput,
  CorrespondenceUserNotificationWhereUniqueInput,
  CorrespondenceUserNotificationCreateInput,
  CorrespondenceUserNotificationUncheckedCreateInput,
  CorrespondenceUserNotificationUpdateInput,
  CorrespondenceUserNotificationUncheckedUpdateInput,
  CorrespondenceUserNotificationOrderByWithRelationInput,
} from '../../prisma/models/CorrespondenceUserNotification';

@Injectable()
export class UserNotificationPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'correspondenceUserNotification',
    UserNotification,
    string,
    UserNotificationFilter,
    CorrespondenceUserNotificationModel,
    CorrespondenceUserNotificationWhereInput,
    CorrespondenceUserNotificationWhereUniqueInput,
    ({} & CorrespondenceUserNotificationCreateInput) | ({} & CorrespondenceUserNotificationUncheckedCreateInput),
    ({} & CorrespondenceUserNotificationUpdateInput) | ({} & CorrespondenceUserNotificationUncheckedUpdateInput),
    CorrespondenceUserNotificationOrderByWithRelationInput
  >
  implements IUserNotificationRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'correspondenceUserNotification');
  }

  async findByUserAndNotification(
    userId: string,
    notificationId: string,
  ): Promise<UserNotification | null> {
    const row = await this.delegate.findFirst({
      where: { userId, notificationId },
    });
    return row ? this.toDomain(row) : null;
  }

  async countUnread(userId: string): Promise<number> {
    return this.delegate.count({
      where: { userId, isRead: false, isArchived: false },
    });
  }

  async markAllReadForUser(userId: string): Promise<void> {
    const now = new Date();
    await this.delegate.updateMany({
      where: { userId, isRead: false },
      data: { isRead: true, readAt: now, updatedAt: now },
    });
  }

  protected toUniqueWhere(id: string): CorrespondenceUserNotificationWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(filter?: UserNotificationFilter): CorrespondenceUserNotificationWhereInput {
    if (!filter) return {};
    return {
      ...(filter.userId ? { userId: filter.userId } : {}),
      ...(filter.notificationId ? { notificationId: filter.notificationId } : {}),
      ...(filter.isRead !== undefined ? { isRead: filter.isRead } : {}),
      ...(filter.isArchived !== undefined ? { isArchived: filter.isArchived } : {}),
      ...(filter.isPushSent !== undefined ? { isPushSent: filter.isPushSent } : {}),
      ...(filter.pushDelivered !== undefined ? { pushDelivered: filter.pushDelivered } : {}),
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
    entity: UserNotification,
  ): ({} & CorrespondenceUserNotificationCreateInput) | ({} & CorrespondenceUserNotificationUncheckedCreateInput) {
    return {
      id: entity.id,
      notificationId: entity.notificationId,
      userId: entity.userId,
      isRead: entity.isRead,
      isArchived: entity.isArchived,
      isPushSent: entity.isPushSent,
      pushDelivered: entity.pushDelivered,
      createdAt: entity.createdAt,
      updatedAt: entity.updatedAt,
    };
  }

  protected toUpdateInput(
    _id: string,
    entity: UserNotification,
  ): ({} & CorrespondenceUserNotificationUpdateInput) | ({} & CorrespondenceUserNotificationUncheckedUpdateInput) {
    return {
      isRead: entity.isRead,
      readAt: entity.readAt ?? null,
      isArchived: entity.isArchived,
      archivedAt: entity.archivedAt ?? null,
      isPushSent: entity.isPushSent,
      pushSentAt: entity.pushSentAt ?? null,
      pushDelivered: entity.pushDelivered,
      pushError: entity.pushError ?? null,
      updatedAt: entity.updatedAt,
    };
  }

  protected defaultOrderBy(): CorrespondenceUserNotificationOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected defaultPageSize(): number {
    return 50;
  }

  protected toDomain(row: CorrespondenceUserNotificationModel): UserNotification {
    return new UserNotification(row.id, row.notificationId, row.userId, {
      isRead: row.isRead,
      readAt: row.readAt ?? undefined,
      isArchived: row.isArchived,
      archivedAt: row.archivedAt ?? undefined,
      isPushSent: row.isPushSent,
      pushSentAt: row.pushSentAt ?? undefined,
      pushDelivered: row.pushDelivered,
      pushError: row.pushError ?? undefined,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    });
  }
}
