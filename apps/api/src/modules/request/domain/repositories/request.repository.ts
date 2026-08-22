import { Page } from '@nabarun-ngo/nestjs-shared-core';
import { RequestEvent, RequestListFilter, RequestRecord } from '../models/request.model';
import { RequestEventType } from '../enums/request-event-type.enum';

export const IRequestRepository = Symbol('IRequestRepository');

export interface CreateRequestPersistence {
  id: string;
  type: string;
  name: string;
  formKey: string;
  formSubmissionId?: string | null;
  status: string;
  initiatedById: string;
  initiatedForId?: string | null;
  executorRoles: string[];
  executorGroups: string[];
  executorPermissions: string[];
  approverRoles: string[];
  approverGroups: string[];
  approverPermissions: string[];
  needApproval: boolean;
}

export interface AppendRequestEventInput {
  requestId: string;
  type: RequestEventType | string;
  actorId?: string | null;
  payload?: Record<string, unknown>;
}

export interface IRequestRepository {
  create(data: CreateRequestPersistence): Promise<RequestRecord>;
  findById(id: string, includeEvents?: boolean): Promise<RequestRecord | null>;
  findPaged(
    filter: RequestListFilter,
    pageIndex: number,
    pageSize: number,
  ): Promise<Page<RequestRecord>>;
  update(id: string, patch: Partial<RequestRecord>): Promise<RequestRecord>;
  appendEvent(input: AppendRequestEventInput): Promise<RequestEvent>;
  countInbox(
    actorRoles: string[],
    actorGroups: string[],
    actorPermissions: string[],
  ): Promise<number>;
}
