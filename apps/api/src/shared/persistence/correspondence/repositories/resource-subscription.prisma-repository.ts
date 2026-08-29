import { Injectable } from '@nestjs/common';
import {
  BasePrismaService,
  PrismaCrudRepositoryBase,
} from '@nabarun-ngo/nestjs-shared-persistence';
import { ResourceSubscription, SubscriptionFilter, SubscriberType, SubscribedVia } from '@nabarun-ngo/nestjs-shared-correspondence/domain/aggregates/resource-subscription.aggregate';
import { SubscriptionChannel } from '@nabarun-ngo/nestjs-shared-correspondence/domain/entities/subscription-channel.entity';
import { ChannelType } from '@nabarun-ngo/nestjs-shared-correspondence/domain/enums/channel-type.enum';
import { EmailRole } from '@nabarun-ngo/nestjs-shared-correspondence/domain/enums/email-role.enum';
import { IResourceSubscriptionRepository } from '@nabarun-ngo/nestjs-shared-correspondence/domain/repositories/resource-subscription.repository';
import type { PrismaClient } from '../../prisma/client';
import type {
  CorrespondenceResourceSubscriptionWhereInput,
  CorrespondenceResourceSubscriptionWhereUniqueInput,
  CorrespondenceResourceSubscriptionCreateInput,
  CorrespondenceResourceSubscriptionUncheckedCreateInput,
  CorrespondenceResourceSubscriptionUpdateInput,
  CorrespondenceResourceSubscriptionUncheckedUpdateInput,
  CorrespondenceResourceSubscriptionOrderByWithRelationInput,
} from '../../prisma/models/CorrespondenceResourceSubscription';

type SubscriptionRow = Awaited<
  ReturnType<PrismaClient['correspondenceResourceSubscription']['findUnique']>
> & {
  channels?: Array<{
    id: string;
    subscriptionId: string;
    channel: string;
    enabled: boolean;
    emailRole: string | null;
    createdAt: Date;
    updatedAt: Date;
  }>;
};

const CHANNEL_INCLUDE = { channels: true } as const;

@Injectable()
export class ResourceSubscriptionPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'correspondenceResourceSubscription',
    ResourceSubscription,
    string,
    SubscriptionFilter,
    NonNullable<SubscriptionRow>,
    CorrespondenceResourceSubscriptionWhereInput,
    CorrespondenceResourceSubscriptionWhereUniqueInput,
    ({} & CorrespondenceResourceSubscriptionCreateInput) | ({} & CorrespondenceResourceSubscriptionUncheckedCreateInput),
    ({} & CorrespondenceResourceSubscriptionUpdateInput) | ({} & CorrespondenceResourceSubscriptionUncheckedUpdateInput),
    CorrespondenceResourceSubscriptionOrderByWithRelationInput,
    typeof CHANNEL_INCLUDE
  >
  implements IResourceSubscriptionRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'correspondenceResourceSubscription');
  }

  override async create(subscription: ResourceSubscription): Promise<ResourceSubscription> {
    const row = await this.delegate.create({
      data: {
        ...this.toCreateInput(subscription),
        channels: {
          create: subscription.channels.map((c) => ({
            id: c.id,
            channel: c.channel,
            enabled: c.enabled,
            emailRole: c.emailRole ?? null,
            createdAt: c.createdAt,
            updatedAt: c.updatedAt,
          })),
        },
      },
      include: CHANNEL_INCLUDE,
    });
    return this.toDomain(row);
  }

  override async update(id: string, subscription: ResourceSubscription): Promise<ResourceSubscription> {
    await this.delegate.update({
      where: { id },
      data: this.toUpdateInput(id, subscription),
    });

    for (const channel of subscription.channels) {
      await this.client.correspondenceSubscriptionChannel.upsert({
        where: { corr_subscriptionChannel_unique: { subscriptionId: id, channel: channel.channel } },
        create: {
          id: channel.id,
          subscriptionId: id,
          channel: channel.channel,
          enabled: channel.enabled,
          emailRole: channel.emailRole ?? null,
          createdAt: channel.createdAt,
          updatedAt: channel.updatedAt,
        },
        update: {
          enabled: channel.enabled,
          emailRole: channel.emailRole ?? null,
          updatedAt: channel.updatedAt,
        },
      });
    }

    return (await this.findById(id))!;
  }

  async findByUserAndResource(
    userId: string,
    resourceType: string,
    resourceId?: string,
  ): Promise<ResourceSubscription | null> {
    const row = await this.delegate.findFirst({
      where: { userId, resourceType, resourceId: resourceId ?? null },
      include: CHANNEL_INCLUDE,
    });
    return row ? this.toDomain(row) : null;
  }

  async findByRoleAndResource(
    roleName: string,
    resourceType: string,
    resourceId?: string,
  ): Promise<ResourceSubscription | null> {
    const row = await this.delegate.findFirst({
      where: { roleName, resourceType, resourceId: resourceId ?? null },
      include: CHANNEL_INCLUDE,
    });
    return row ? this.toDomain(row) : null;
  }

  async findActiveSubscribersForResource(
    resourceType: string,
    resourceId?: string,
  ): Promise<ResourceSubscription[]> {
    const rows = await this.delegate.findMany({
      where: {
        resourceType,
        resourceId: resourceId ?? null,
        isActive: true,
      },
      include: CHANNEL_INCLUDE,
    });
    return rows.map((row) => this.toDomain(row));
  }

  async updateEmailForUser(userId: string, newEmail: string): Promise<void> {
    await this.delegate.updateMany({
      where: { userId },
      data: { userEmail: newEmail, updatedAt: new Date() },
    });
  }

  async deleteInactiveBefore(date: Date): Promise<number> {
    const result = await this.delegate.deleteMany({
      where: { isActive: false, updatedAt: { lt: date } },
    });
    return result.count;
  }

  protected toUniqueWhere(id: string): CorrespondenceResourceSubscriptionWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(filter?: SubscriptionFilter): CorrespondenceResourceSubscriptionWhereInput {
    if (!filter) return {};
    return {
      ...(filter.userId ? { userId: filter.userId } : {}),
      ...(filter.roleName ? { roleName: filter.roleName } : {}),
      ...(filter.resourceType ? { resourceType: filter.resourceType } : {}),
      ...(filter.resourceId !== undefined ? { resourceId: filter.resourceId } : {}),
      ...(filter.isActive !== undefined ? { isActive: filter.isActive } : {}),
      ...(filter.subscriberType ? { subscriberType: filter.subscriberType } : {}),
    };
  }

  protected toCreateInput(
    subscription: ResourceSubscription,
  ): ({} & CorrespondenceResourceSubscriptionCreateInput) | ({} & CorrespondenceResourceSubscriptionUncheckedCreateInput) {
    return {
      id: subscription.id,
      subscriberType: subscription.subscriberType,
      userId: subscription.userId ?? null,
      userEmail: subscription.userEmail ?? null,
      userName: subscription.userName ?? null,
      roleName: subscription.roleName ?? null,
      resourceType: subscription.resourceType,
      resourceId: subscription.resourceId ?? null,
      subscribedVia: subscription.subscribedVia,
      isActive: subscription.isActive,
      createdAt: subscription.createdAt,
      updatedAt: subscription.updatedAt,
    };
  }

  protected toUpdateInput(
    _id: string,
    subscription: ResourceSubscription,
  ): ({} & CorrespondenceResourceSubscriptionUpdateInput) | ({} & CorrespondenceResourceSubscriptionUncheckedUpdateInput) {
    return {
      isActive: subscription.isActive,
      userEmail: subscription.userEmail ?? null,
      updatedAt: subscription.updatedAt,
    };
  }

  protected defaultOrderBy(): CorrespondenceResourceSubscriptionOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected defaultPageSize(): number {
    return 50;
  }

  protected override toInclude(): typeof CHANNEL_INCLUDE {
    return CHANNEL_INCLUDE;
  }

  protected toDomain(row: NonNullable<SubscriptionRow>): ResourceSubscription {
    const channels: SubscriptionChannel[] = (row.channels ?? []).map(
      (c) =>
        new SubscriptionChannel(c.id, c.subscriptionId, c.channel as ChannelType, {
          enabled: c.enabled,
          emailRole: (c.emailRole as EmailRole) ?? undefined,
          createdAt: c.createdAt,
          updatedAt: c.updatedAt,
        }),
    );
    return new ResourceSubscription(
      row.id,
      row.subscriberType as SubscriberType,
      row.resourceType,
      row.subscribedVia as SubscribedVia,
      {
        userId: row.userId ?? undefined,
        userEmail: row.userEmail ?? undefined,
        userName: row.userName ?? undefined,
        roleName: row.roleName ?? undefined,
        resourceId: row.resourceId ?? undefined,
        isActive: row.isActive,
        channels,
        createdAt: row.createdAt,
        updatedAt: row.updatedAt,
      },
    );
  }
}
