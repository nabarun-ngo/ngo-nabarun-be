import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { PrismaClient } from '../../prisma/client';
import type {
  ActivityWhereInput,
  ActivityWhereUniqueInput,
  ActivityUncheckedCreateInput,
  ActivityUncheckedUpdateInput,
  ActivityOrderByWithRelationInput,
} from '../../prisma/models/Activity';
import { Activity, ActivityFilter } from '../../../../modules/project/domain/aggregates/activity/activity.aggregate';
import { IActivityRepository } from '../../../../modules/project/domain/repositories/activity.repository';
import { ActivityPersistence, ActivityPrismaMapper } from '../mapper/activity-prisma.mapper';

const ACTIVITY_RELATIONS = {
  project: true,
  assignee: true,
  organizer: true,
  parentActivity: true,
} as const;

@Injectable()
export class ActivityPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'activity',
    Activity,
    string,
    ActivityFilter,
    ActivityPersistence,
    ActivityWhereInput,
    ActivityWhereUniqueInput,
    ActivityUncheckedCreateInput,
    ActivityUncheckedUpdateInput,
    ActivityOrderByWithRelationInput,
    typeof ACTIVITY_RELATIONS
  >
  implements IActivityRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'activity');
  }

  protected toDomain(row: ActivityPersistence): Activity {
    return ActivityPrismaMapper.toDomain(row)!;
  }

  protected toCreateInput(entity: Activity): ActivityUncheckedCreateInput {
    return ActivityPrismaMapper.toCreate(entity);
  }

  protected toUpdateInput(_id: string, entity: Activity): ActivityUncheckedUpdateInput {
    return ActivityPrismaMapper.toUpdate(entity);
  }

  protected toUniqueWhere(id: string): ActivityWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(props?: ActivityFilter): ActivityWhereInput {
    return {
      ...(props?.projectId ? { projectId: props.projectId } : {}),
      ...(props?.scale ? { scale: props.scale } : {}),
      ...(props?.status ? { status: props.status } : {}),
      ...(props?.type ? { type: props.type } : {}),
      ...(props?.assignedTo ? { assignedTo: props.assignedTo } : {}),
      ...(props?.organizerId ? { organizerId: props.organizerId } : {}),
      ...(props?.parentActivityId ? { parentActivityId: props.parentActivityId } : {}),
      deletedAt: null,
    };
  }

  protected override defaultOrderBy(): ActivityOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected override toInclude(): typeof ACTIVITY_RELATIONS {
    return ACTIVITY_RELATIONS;
  }

  protected override defaultPageSize(): number {
    return 20;
  }

  async findRecentSummariesByProjectId(projectId: string, limit: number) {
    return this.delegate.findMany({
      where: { projectId, deletedAt: null },
      orderBy: { updatedAt: 'desc' },
      take: limit,
      select: { id: true, name: true, status: true, scale: true },
    });
  }

  override async create(entity: Activity): Promise<Activity> {
    const row = await this.delegate.create({
      data: ActivityPrismaMapper.toCreate(entity),
      include: ACTIVITY_RELATIONS,
    });
    return ActivityPrismaMapper.toDomain(row)!;
  }

  override async update(id: string, entity: Activity): Promise<Activity> {
    const row = await this.delegate.update({
      where: { id },
      data: ActivityPrismaMapper.toUpdate(entity),
      include: ACTIVITY_RELATIONS,
    });
    return ActivityPrismaMapper.toDomain(row)!;
  }

  override async delete(id: string): Promise<void> {
    await this.delegate.update({ where: { id }, data: { deletedAt: new Date() } });
  }
}
