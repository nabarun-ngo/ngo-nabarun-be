import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { Prisma, PrismaClient } from '../../prisma/client';
import type {
  GoalWhereInput,
  GoalWhereUniqueInput,
  GoalUncheckedCreateInput,
  GoalUncheckedUpdateInput,
  GoalOrderByWithRelationInput,
} from '../../prisma/models/Goal';
import { IGoalRepository } from '../../../../modules/project/domain/repositories/goal.repository';
import { Goal, GoalFilter } from '../../../../modules/project/domain/aggregates/goal/goal.aggregate';
import { GoalPrismaMapper } from '../mapper/goal-prisma.mapper';

type GoalRow = Prisma.GoalGetPayload<object>;

@Injectable()
export class GoalPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'goal',
    Goal,
    string,
    GoalFilter,
    GoalRow,
    GoalWhereInput,
    GoalWhereUniqueInput,
    GoalUncheckedCreateInput,
    GoalUncheckedUpdateInput,
    GoalOrderByWithRelationInput
  >
  implements IGoalRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'goal');
  }

  protected toDomain(row: GoalRow): Goal {
    return GoalPrismaMapper.toDomain(row)!;
  }

  protected toCreateInput(entity: Goal): GoalUncheckedCreateInput {
    return GoalPrismaMapper.toCreate(entity);
  }

  protected toUpdateInput(_id: string, entity: Goal): GoalUncheckedUpdateInput {
    return GoalPrismaMapper.toUpdate(entity);
  }

  protected toUniqueWhere(id: string): GoalWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(props?: GoalFilter): GoalWhereInput {
    return {
      deletedAt: null,
      ...(props?.projectId ? { projectId: props.projectId } : {}),
      ...(props?.status ? { status: props.status } : {}),
      ...(props?.priority ? { priority: props.priority } : {}),
    };
  }

  protected override defaultOrderBy(): GoalOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected override defaultPageSize(): number {
    return 20;
  }

  override async delete(id: string): Promise<void> {
    await this.delegate.update({ where: { id }, data: { deletedAt: new Date() } });
  }
}
