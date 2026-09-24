import { RequestStatus } from '../enums/request-status.enum';
import { RequestEventType } from '../enums/request-event-type.enum';

export interface RequestPersonRef {
  id: string;
  firstName?: string | null;
  lastName?: string | null;
}

export interface RequestEvent {
  id: string;
  requestId: string;
  type: RequestEventType | string;
  actorId?: string | null;
  payload: Record<string, unknown>;
  occurredAt: Date;
}

export interface RequestRecord {
  id: string;
  type: string;
  name: string;
  formKey: string;
  formSubmissionId?: string | null;
  status: RequestStatus | string;
  initiatedById?: string | null;
  initiatedForId?: string | null;
  assigneeId?: string | null;
  claimedById?: string | null;
  claimedAt?: Date | null;
  executorRoles: string[];
  executorGroups: string[];
  executorPermissions: string[];
  approverRoles: string[];
  approverGroups: string[];
  approverPermissions: string[];
  needApproval: boolean;
  completedAt?: Date | null;
  decisionNote?: string | null;
  createdAt: Date;
  updatedAt: Date;
  initiatedBy?: RequestPersonRef | null;
  initiatedFor?: RequestPersonRef | null;
  assignee?: RequestPersonRef | null;
  claimedBy?: RequestPersonRef | null;
  events?: RequestEvent[];
}

export type RequestListScope = 'mine' | 'inbox' | 'started';

export interface RequestListFilter {
  scope: RequestListScope;
  type?: string;
  status?: string;
  id?: string;
  actorUserId: string;
  actorRoles: string[];
  actorGroups: string[];
  actorPermissions: string[];
}

export interface CreateRequestInput {
  type: string;
  initiatedById: string;
  initiatedForId?: string | null;
  formValues: Record<string, unknown>;
}
