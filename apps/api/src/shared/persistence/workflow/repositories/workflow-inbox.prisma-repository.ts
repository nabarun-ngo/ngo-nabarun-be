import { Injectable } from '@nestjs/common';
import {
  BasePrismaService,
  PrismaCrudRepositoryBase,
} from '@nabarun-ngo/nestjs-shared-persistence';
import {
  IWorkflowInboxRepository,
  InboxTaskStatus,
  WorkflowInboxFilter,
  WorkflowInboxTaskRecord,
  WorkflowTaskNotClaimableError,
  WorkflowTaskNotCompletableError,
} from '@nabarun-ngo/nestjs-shared-workflow';
import { Prisma, PrismaClient } from '../../prisma/client';
import {
  WorkflowTaskInboxWhereInput,
  WorkflowTaskInboxWhereUniqueInput,
  WorkflowTaskInboxOrderByWithRelationInput,
} from '../../prisma/models/WorkflowTaskInbox';

type WorkflowTaskInboxRow = {
  id: string;
  instanceId: string;
  elementId: string;
  workflowType: string;
  formKey: string | null;
  status: string;
  assignedToId: string | null;
  candidateRoleNames: Prisma.JsonValue;
  slaDeadlineAt: Date | null;
  claimedAt: Date | null;
  claimedById: string | null;
  completedAt: Date | null;
  createdAt: Date;
  updatedAt: Date;
};

const OPEN_STATUSES: InboxTaskStatus[] = [
  InboxTaskStatus.Pending,
  InboxTaskStatus.Claimed,
];

@Injectable()
export class WorkflowInboxPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'workflowTaskInbox',
    WorkflowInboxTaskRecord,
    string,
    WorkflowInboxFilter,
    WorkflowTaskInboxRow,
    WorkflowTaskInboxWhereInput,
    WorkflowTaskInboxWhereUniqueInput,
    any,
    any,
    WorkflowTaskInboxOrderByWithRelationInput
  >
  implements IWorkflowInboxRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'workflowTaskInbox');
  }

  async findByInstanceAndElement(
    instanceId: string,
    elementId: string,
  ): Promise<WorkflowInboxTaskRecord | null> {
    const row = await this.client.workflowTaskInbox.findUnique({
      where: { instanceId_elementId: { instanceId, elementId } },
    });

    return row ? this.toDomain(row as WorkflowTaskInboxRow) : null;
  }

  async findOpenForUser(userId: string): Promise<WorkflowInboxTaskRecord[]> {
    const rows = await this.client.workflowTaskInbox.findMany({
      where: {
        assignedToId: userId,
        status: { in: OPEN_STATUSES },
      },
      orderBy: { createdAt: 'asc' },
    });

    return rows.map((row) => this.toDomain(row as WorkflowTaskInboxRow));
  }

  async findOpenUnassigned(): Promise<WorkflowInboxTaskRecord[]> {
    const rows = await this.client.workflowTaskInbox.findMany({
      where: {
        assignedToId: null,
        status: { in: OPEN_STATUSES },
      },
      orderBy: { createdAt: 'asc' },
    });

    return rows.map((row) => this.toDomain(row as WorkflowTaskInboxRow));
  }

  async reopenTask(params: {
    taskId: string;
    assignedToId: string | null;
    candidateRoleNames: string[];
    formKey: string | null;
    slaDeadlineAt: Date | null;
  }): Promise<WorkflowInboxTaskRecord> {
    const now = new Date();
    const row = await this.client.workflowTaskInbox.update({
      where: { id: params.taskId },
      data: {
        status: InboxTaskStatus.Pending,
        assignedToId: params.assignedToId,
        candidateRoleNames: params.candidateRoleNames as Prisma.InputJsonValue,
        formKey: params.formKey,
        slaDeadlineAt: params.slaDeadlineAt,
        claimedAt: null,
        claimedById: null,
        completedAt: null,
        updatedAt: now,
      },
    });

    return this.toDomain(row as WorkflowTaskInboxRow);
  }

  async claimTask(params: {
    taskId: string;
    claimedById: string;
    expectedStatus: InboxTaskStatus;
  }): Promise<WorkflowInboxTaskRecord> {
    const now = new Date();
    const result = await this.client.workflowTaskInbox.updateMany({
      where: { id: params.taskId, status: params.expectedStatus },
      data: {
        status: InboxTaskStatus.Claimed,
        claimedById: params.claimedById,
        claimedAt: now,
        assignedToId: params.claimedById,
        updatedAt: now,
      },
    });

    if (result.count === 0) {
      throw new WorkflowTaskNotClaimableError(params.taskId);
    }

    const task = await this.findById(params.taskId);
    return task!;
  }

  async completeTask(params: {
    taskId: string;
    completedById: string;
    expectedStatus: InboxTaskStatus;
  }): Promise<WorkflowInboxTaskRecord> {
    const now = new Date();
    const result = await this.client.workflowTaskInbox.updateMany({
      where: { id: params.taskId, status: params.expectedStatus },
      data: {
        status: InboxTaskStatus.Completed,
        completedAt: now,
        updatedAt: now,
      },
    });

    if (result.count === 0) {
      throw new WorkflowTaskNotCompletableError(params.taskId);
    }

    const task = await this.findById(params.taskId);
    return task!;
  }

  async releaseTasksForDeletedUser(userId: string): Promise<number> {
    const result = await this.client.workflowTaskInbox.updateMany({
      where: {
        assignedToId: userId,
        status: { in: OPEN_STATUSES },
      },
      data: {
        assignedToId: null,
        claimedById: null,
        claimedAt: null,
        status: InboxTaskStatus.Pending,
        updatedAt: new Date(),
      },
    });

    return result.count;
  }

  protected toDomain(row: WorkflowTaskInboxRow): WorkflowInboxTaskRecord {
    return {
      id: row.id,
      instanceId: row.instanceId,
      elementId: row.elementId,
      workflowType: row.workflowType,
      formKey: row.formKey,
      status: row.status as InboxTaskStatus,
      assignedToId: row.assignedToId,
      candidateRoleNames: (row.candidateRoleNames ?? []) as string[],
      slaDeadlineAt: row.slaDeadlineAt,
      claimedAt: row.claimedAt,
      claimedById: row.claimedById,
      completedAt: row.completedAt,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    };
  }

  protected toCreateInput(entity: WorkflowInboxTaskRecord): any {
    return {
      id: entity.id,
      instanceId: entity.instanceId,
      elementId: entity.elementId,
      workflowType: entity.workflowType,
      formKey: entity.formKey,
      status: entity.status,
      assignedToId: entity.assignedToId,
      candidateRoleNames: entity.candidateRoleNames as Prisma.InputJsonValue,
      slaDeadlineAt: entity.slaDeadlineAt,
      claimedAt: entity.claimedAt,
      claimedById: entity.claimedById,
      completedAt: entity.completedAt,
      createdAt: entity.createdAt,
      updatedAt: entity.updatedAt,
    };
  }

  protected toUpdateInput(_id: string, entity: WorkflowInboxTaskRecord): any {
    return {
      workflowType: entity.workflowType,
      formKey: entity.formKey,
      status: entity.status,
      assignedToId: entity.assignedToId,
      candidateRoleNames: entity.candidateRoleNames as Prisma.InputJsonValue,
      slaDeadlineAt: entity.slaDeadlineAt,
      claimedAt: entity.claimedAt,
      claimedById: entity.claimedById,
      completedAt: entity.completedAt,
      updatedAt: entity.updatedAt,
    };
  }

  protected toUniqueWhere(id: string): WorkflowTaskInboxWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(filter?: WorkflowInboxFilter): WorkflowTaskInboxWhereInput {
    return {
      ...(filter?.assignedToId ? { assignedToId: filter.assignedToId } : {}),
      ...(filter?.status ? { status: filter.status } : {}),
      ...(filter?.instanceId ? { instanceId: filter.instanceId } : {}),
      ...(filter?.workflowType ? { workflowType: filter.workflowType } : {}),
      ...(filter?.overdueBefore
        ? { slaDeadlineAt: { lt: filter.overdueBefore } }
        : {}),
    };
  }

  protected defaultOrderBy(): WorkflowTaskInboxOrderByWithRelationInput {
    return { createdAt: 'asc' };
  }
}
