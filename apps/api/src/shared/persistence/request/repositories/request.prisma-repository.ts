import { Injectable } from '@nestjs/common';
import { Page } from '@nabarun-ngo/nestjs-shared-core';
import { BasePrismaService } from '@nabarun-ngo/nestjs-shared-persistence';
import { Prisma, PrismaClient } from '../../prisma/client';
import { RequestStatus } from '../../../../modules/request/domain/enums/request-status.enum';
import {
  AppendRequestEventInput,
  CreateRequestPersistence,
  IRequestRepository,
} from '../../../../modules/request/domain/repositories/request.repository';
import {
  RequestEvent,
  RequestListFilter,
  RequestRecord,
} from '../../../../modules/request/domain/models/request.model';
import {
  RequestPrismaMapper,
  requestIncludeUsers,
  requestIncludeWithEvents,
} from '../mappers/request-prisma.mapper';

@Injectable()
export class RequestPrismaRepository implements IRequestRepository {
  constructor(private readonly database: BasePrismaService<PrismaClient>) {}

  async create(data: CreateRequestPersistence): Promise<RequestRecord> {
    const row = await this.database.client.request.create({
      data: {
        id: data.id,
        type: data.type,
        name: data.name,
        formKey: data.formKey,
        formSubmissionId: data.formSubmissionId ?? null,
        status: data.status,
        initiatedById: data.initiatedById,
        initiatedForId: data.initiatedForId ?? null,
        executorRoles: data.executorRoles,
        executorGroups: data.executorGroups,
        executorPermissions: data.executorPermissions,
        approverRoles: data.approverRoles,
        approverGroups: data.approverGroups,
        approverPermissions: data.approverPermissions,
        needApproval: data.needApproval,
      },
      include: requestIncludeUsers,
    });
    return RequestPrismaMapper.toDomain(row)!;
  }

  async findById(id: string, includeEvents = false): Promise<RequestRecord | null> {
    const row = await this.database.client.request.findUnique({
      where: { id },
      include: includeEvents ? requestIncludeWithEvents : requestIncludeUsers,
    });
    return RequestPrismaMapper.toDomain(row);
  }

  async findPaged(
    filter: RequestListFilter,
    pageIndex: number,
    pageSize: number,
  ): Promise<Page<RequestRecord>> {
    const where = this.whereQuery(filter);
    const total = await this.database.client.request.count({ where });

    let rows: Prisma.RequestGetPayload<{ include: typeof requestIncludeUsers }>[];

    if (filter.scope === 'inbox') {
      const orderedIds = await this.findInboxOrderedIds(filter, pageIndex, pageSize);
      if (!orderedIds.length) {
        rows = [];
      } else {
        const fetched = await this.database.client.request.findMany({
          where: { id: { in: orderedIds } },
          include: requestIncludeUsers,
        });
        const byId = new Map(fetched.map((row) => [row.id, row]));
        rows = orderedIds.map((id) => byId.get(id)!).filter(Boolean);
      }
    } else {
      rows = await this.database.client.request.findMany({
        where,
        include: requestIncludeUsers,
        orderBy: { createdAt: 'desc' },
        skip: pageIndex * pageSize,
        take: pageSize,
      });
    }

    return new Page(
      rows.map((row) => RequestPrismaMapper.toDomain(row)!),
      total,
      pageIndex,
      pageSize,
    );
  }

  async update(id: string, patch: Partial<RequestRecord>): Promise<RequestRecord> {
    const data: Prisma.RequestUpdateInput = {};
    if (patch.status !== undefined) data.status = patch.status;
    if (patch.formSubmissionId !== undefined) data.formSubmissionId = patch.formSubmissionId;
    if (patch.assigneeId !== undefined) {
      data.assignee = patch.assigneeId
        ? { connect: { id: patch.assigneeId } }
        : { disconnect: true };
    }
    if (patch.claimedById !== undefined) {
      data.claimedBy = patch.claimedById
        ? { connect: { id: patch.claimedById } }
        : { disconnect: true };
    }
    if (patch.claimedAt !== undefined) data.claimedAt = patch.claimedAt;
    if (patch.completedAt !== undefined) data.completedAt = patch.completedAt;
    if (patch.decisionNote !== undefined) data.decisionNote = patch.decisionNote;

    const row = await this.database.client.request.update({
      where: { id },
      data,
      include: requestIncludeUsers,
    });
    return RequestPrismaMapper.toDomain(row)!;
  }

  async appendEvent(input: AppendRequestEventInput): Promise<RequestEvent> {
    const row = await this.database.client.requestEvent.create({
      data: {
        requestId: input.requestId,
        type: input.type,
        actorId: input.actorId ?? null,
        payload: (input.payload ?? {}) as Prisma.InputJsonValue,
      },
    });
    return {
      id: row.id,
      requestId: row.requestId,
      type: row.type,
      actorId: row.actorId,
      payload: (row.payload as Record<string, unknown>) ?? {},
      occurredAt: row.occurredAt,
    };
  }

  async countInbox(
    actorRoles: string[],
    actorGroups: string[],
    actorPermissions: string[],
  ): Promise<number> {
    return this.database.client.request.count({
      where: this.inboxWhere(actorRoles, actorGroups, actorPermissions),
    });
  }

  private async findInboxOrderedIds(
    filter: RequestListFilter,
    pageIndex: number,
    pageSize: number,
  ): Promise<string[]> {
    const where = this.whereQuery(filter);
    // Fetch candidates then apply priority sort in memory within the page window.
    // Inbox sizes are expected to stay modest; priority = assigned-to-me at PendingForApproval.
    const candidates = await this.database.client.request.findMany({
      where,
      select: { id: true, status: true, assigneeId: true, createdAt: true },
      orderBy: { createdAt: 'desc' },
    });

    candidates.sort((a, b) => {
      const aPriority =
        a.status === RequestStatus.PendingForApproval && a.assigneeId === filter.actorUserId
          ? 0
          : 1;
      const bPriority =
        b.status === RequestStatus.PendingForApproval && b.assigneeId === filter.actorUserId
          ? 0
          : 1;
      if (aPriority !== bPriority) return aPriority - bPriority;
      return b.createdAt.getTime() - a.createdAt.getTime();
    });

    return candidates.slice(pageIndex * pageSize, pageIndex * pageSize + pageSize).map((r) => r.id);
  }

  private whereQuery(filter: RequestListFilter): Prisma.RequestWhereInput {
    const and: Prisma.RequestWhereInput[] = [];

    if (filter.type) and.push({ type: filter.type });
    if (filter.status) and.push({ status: filter.status });
    if (filter.id) and.push({ id: { contains: filter.id } });

    if (filter.scope === 'mine') {
      and.push({
        OR: [
          { initiatedById: filter.actorUserId },
          { initiatedForId: filter.actorUserId },
        ],
      });
    } else if (filter.scope === 'started') {
      and.push({
        status: RequestStatus.InProgress,
        OR: [
          { claimedById: filter.actorUserId },
          { assigneeId: filter.actorUserId },
        ],
      });
    } else {
      and.push(this.inboxWhere(
        filter.actorRoles,
        filter.actorGroups,
        filter.actorPermissions,
      ));
    }

    return and.length ? { AND: and } : {};
  }

  private inboxWhere(
    actorRoles: string[],
    actorGroups: string[],
    actorPermissions: string[],
  ): Prisma.RequestWhereInput {
    const executorRoleFilters = actorRoles.map((role) => ({
      executorRoles: { array_contains: [role] },
    }));
    const executorGroupFilters = actorGroups.map((group) => ({
      executorGroups: { array_contains: [group] },
    }));
    const executorPermissionFilters = actorPermissions.map((permission) => ({
      executorPermissions: { array_contains: [permission] },
    }));
    const approverRoleFilters = actorRoles.map((role) => ({
      approverRoles: { array_contains: [role] },
    }));
    const approverGroupFilters = actorGroups.map((group) => ({
      approverGroups: { array_contains: [group] },
    }));
    const approverPermissionFilters = actorPermissions.map((permission) => ({
      approverPermissions: { array_contains: [permission] },
    }));

    const executorEligibility =
      executorRoleFilters.length
      || executorGroupFilters.length
      || executorPermissionFilters.length
        ? {
          OR: [
            ...executorRoleFilters,
            ...executorGroupFilters,
            ...executorPermissionFilters,
          ],
        }
        : null;

    const approverEligibility =
      approverRoleFilters.length
      || approverGroupFilters.length
      || approverPermissionFilters.length
        ? {
          OR: [
            ...approverRoleFilters,
            ...approverGroupFilters,
            ...approverPermissionFilters,
          ],
        }
        : null;

    const branches: Prisma.RequestWhereInput[] = [];

    if (approverEligibility) {
      branches.push({
        AND: [{ status: RequestStatus.PendingForApproval }, approverEligibility],
      });
    }

    if (executorEligibility) {
      branches.push({
        AND: [
          { status: RequestStatus.YetToStart },
          { claimedById: null },
          executorEligibility,
        ],
      });
    }

    if (!branches.length) {
      return { id: '__no_match__' };
    }

    return { OR: branches };
  }
}
