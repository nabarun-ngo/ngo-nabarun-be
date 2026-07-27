import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { Inject, Injectable } from '@nestjs/common';
import { GetMyInboxQuery } from './get-my-inbox.query';
import { IWorkflowInboxRepository } from '../../../domain/ports/workflow-inbox.repository';
import type { WorkflowInboxTaskRecord } from '../../../domain/ports/workflow-inbox.repository';
import {
  IWorkflowUserResolutionPort,
  WORKFLOW_USER_RESOLUTION_PORT,
} from '../../../domain/ports/workflow-user-resolution.port';

@QueryHandler(GetMyInboxQuery)
@Injectable()
export class GetMyInboxHandler implements IQueryHandler<GetMyInboxQuery, WorkflowInboxTaskRecord[]> {
  constructor(
    @Inject(IWorkflowInboxRepository)
    private readonly inboxRepo: IWorkflowInboxRepository,
    @Inject(WORKFLOW_USER_RESOLUTION_PORT)
    private readonly userResolution: IWorkflowUserResolutionPort,
  ) {}

  async execute(query: GetMyInboxQuery): Promise<WorkflowInboxTaskRecord[]> {
    const assigned = await this.inboxRepo.findOpenForUser(query.userId);
    const assignedIds = new Set(assigned.map((task) => task.id));
    const eligible: WorkflowInboxTaskRecord[] = [...assigned];

    const unassigned = await this.inboxRepo.findOpenUnassigned();
    for (const task of unassigned) {
      if (assignedIds.has(task.id)) {
        continue;
      }

      const allowed = await this.userResolution.canUserActOnTask({
        userId: query.userId,
        userPermissions: query.userPermissions,
        instanceId: task.instanceId,
        elementId: task.elementId,
        assignedToId: task.assignedToId,
        candidateRoleNames: task.candidateRoleNames,
      });

      if (allowed) {
        eligible.push(task);
      }
    }

    return eligible.sort((a, b) => a.createdAt.getTime() - b.createdAt.getTime());
  }
}
