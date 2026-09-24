import { RequestDefinition } from '../../request-definition.schema';
import { RequestStatus } from '../../domain/enums/request-status.enum';
import { RequestEvent, RequestRecord } from '../../domain/models/request.model';
import {
  RequestDto,
  RequestEventDto,
  RequestPersonDto,
  RequestTypeDto,
} from '../dtos/request.dto';

function mapPerson(
  person?: { id: string; firstName?: string | null; lastName?: string | null } | null,
): RequestPersonDto | null {
  if (!person) return null;
  return {
    id: person.id,
    firstName: person.firstName ?? null,
    lastName: person.lastName ?? null,
  };
}

function mapEvent(event: RequestEvent): RequestEventDto {
  return {
    id: event.id,
    requestId: event.requestId,
    type: event.type,
    actorId: event.actorId ?? null,
    payload: event.payload ?? {},
    occurredAt: event.occurredAt,
  };
}

export function toRequestTypeDto(def: RequestDefinition): RequestTypeDto {
  return {
    id: def.id,
    version: def.version,
    name: def.name,
    description: def.description,
    formKey: def.formKey,
    executorInstructions: def.executorInstructions,
    needApproval: def.needApproval ?? false,
    approvers: {
      roles: def.approvers?.roles ?? [],
      permissions: def.approvers?.permissions ?? [],
      groups: def.approvers?.groups ?? [],
    },
    executors: {
      roles: def.executors?.roles ?? [],
      permissions: def.executors?.permissions ?? [],
      groups: def.executors?.groups ?? [],
    },
  };
}

export function toRequestDto(
  record: RequestRecord,
  extras?: {
    executorInstructions?: string | null;
    actorUserId?: string;
  },
): RequestDto {
  const assignedToMeAtApproval =
    !!extras?.actorUserId &&
    record.status === RequestStatus.PendingForApproval &&
    record.assigneeId === extras.actorUserId;

  return {
    id: record.id,
    type: record.type,
    name: record.name,
    formKey: record.formKey,
    formSubmissionId: record.formSubmissionId ?? null,
    status: record.status,
    initiatedById: record.initiatedById ?? null,
    initiatedForId: record.initiatedForId ?? null,
    assigneeId: record.assigneeId ?? null,
    claimedById: record.claimedById ?? null,
    claimedAt: record.claimedAt ?? null,
    executorRoles: record.executorRoles ?? [],
    executorGroups: record.executorGroups ?? [],
    executorPermissions: record.executorPermissions ?? [],
    needApproval: record.needApproval,
    executorInstructions: extras?.executorInstructions ?? null,
    approverRoles: record.approverRoles ?? [],
    approverGroups: record.approverGroups ?? [],
    approverPermissions: record.approverPermissions ?? [],
    assignedToMeAtApproval,
    completedAt: record.completedAt ?? null,
    decisionNote: record.decisionNote ?? null,
    createdAt: record.createdAt,
    updatedAt: record.updatedAt,
    initiatedBy: mapPerson(record.initiatedBy),
    initiatedFor: mapPerson(record.initiatedFor),
    assignee: mapPerson(record.assignee),
    claimedBy: mapPerson(record.claimedBy),
    events: record.events?.map(mapEvent),
  };
}
