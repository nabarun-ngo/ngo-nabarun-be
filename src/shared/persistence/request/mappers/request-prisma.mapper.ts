import { Prisma } from '../../prisma/client';
import { RequestEvent, RequestPersonRef, RequestRecord } from '../../../../modules/request/domain/models/request.model';

const personSelect = { id: true, firstName: true, lastName: true } as const;

export const requestIncludeUsers = {
  initiatedBy: { select: personSelect },
  initiatedFor: { select: personSelect },
  assignee: { select: personSelect },
  claimedBy: { select: personSelect },
} as const;

export const requestIncludeWithEvents = {
  ...requestIncludeUsers,
  events: { orderBy: { occurredAt: 'asc' as const } },
} as const;

type RequestRow = Prisma.RequestGetPayload<{ include: typeof requestIncludeUsers }>;
type RequestRowWithEvents = Prisma.RequestGetPayload<{ include: typeof requestIncludeWithEvents }>;

function asStringArray(value: unknown): string[] {
  if (!Array.isArray(value)) return [];
  return value.filter((v): v is string => typeof v === 'string');
}

function toPerson(row?: { id: string; firstName: string | null; lastName: string | null } | null): RequestPersonRef | null {
  if (!row) return null;
  return { id: row.id, firstName: row.firstName, lastName: row.lastName };
}

export class RequestPrismaMapper {
  static toDomain(row: RequestRow | RequestRowWithEvents | null): RequestRecord | null {
    if (!row) return null;
    const events =
      'events' in row && Array.isArray(row.events)
        ? row.events.map(
            (e): RequestEvent => ({
              id: e.id,
              requestId: e.requestId,
              type: e.type,
              actorId: e.actorId,
              payload: (e.payload as Record<string, unknown>) ?? {},
              occurredAt: e.occurredAt,
            }),
          )
        : undefined;

    return {
      id: row.id,
      type: row.type,
      name: row.name,
      formKey: row.formKey,
      formSubmissionId: row.formSubmissionId,
      status: row.status,
      initiatedById: row.initiatedById,
      initiatedForId: row.initiatedForId,
      assigneeId: row.assigneeId,
      claimedById: row.claimedById,
      claimedAt: row.claimedAt,
      executorRoles: asStringArray(row.executorRoles),
      executorGroups: asStringArray(row.executorGroups),
      executorPermissions: asStringArray(row.executorPermissions),
      approverRoles: asStringArray(row.approverRoles),
      approverGroups: asStringArray(row.approverGroups),
      approverPermissions: asStringArray(row.approverPermissions),
      needApproval: row.needApproval,
      completedAt: row.completedAt,
      decisionNote: row.decisionNote,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
      initiatedBy: toPerson(row.initiatedBy),
      initiatedFor: toPerson(row.initiatedFor),
      assignee: toPerson(row.assignee),
      claimedBy: toPerson(row.claimedBy),
      events,
    };
  }
}
