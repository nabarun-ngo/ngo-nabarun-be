import { Injectable } from '@nestjs/common';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import type { WorkflowInstanceRecord } from '../../domain/ports/workflow-instance.repository';
import type { WorkflowInboxTaskRecord } from '../../domain/ports/workflow-inbox.repository';
import type { WorkflowEventLogEntry } from '../../domain/ports/workflow-event-log.repository';
import type { StartWorkflowParams, CompleteUserTaskParams } from './workflow-orchestrator.service';
import type { WorkflowDefinition } from '../../dsl/workflow-definition.schema';
import { StartWorkflowCommand } from '../commands/start-workflow/start-workflow.command';
import { CancelWorkflowCommand } from '../commands/cancel-workflow/cancel-workflow.command';
import { CompleteUserTaskCommand } from '../commands/complete-user-task/complete-user-task.command';
import { ClaimTaskCommand } from '../commands/claim-task/claim-task.command';
import { DelegateTaskCommand } from '../commands/delegate-task/delegate-task.command';
import { GetWorkflowInstanceQuery } from '../queries/get-workflow-instance/get-workflow-instance.query';
import { GetMyInboxQuery } from '../queries/get-my-inbox/get-my-inbox.query';
import { GetWorkflowTimelineQuery } from '../queries/get-workflow-timeline/get-workflow-timeline.query';
import { GetWorkflowDefinitionQuery } from '../queries/get-workflow-definition/get-workflow-definition.query';
import { ReleaseUserInboxTasksCommand } from '../commands/release-user-inbox-tasks/release-user-inbox-tasks.command';

@Injectable()
export class WorkflowFacade {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) {}

  getDefinition(definitionId: string, version?: number): Promise<WorkflowDefinition> {
    return this.queryBus.execute(new GetWorkflowDefinitionQuery(definitionId, version));
  }

  startWorkflow(params: StartWorkflowParams): Promise<WorkflowInstanceRecord> {
    return this.commandBus.execute(new StartWorkflowCommand(params));
  }

  cancelWorkflow(params: {
    instanceId: string;
    actorId?: string | null;
    remarks?: string;
  }): Promise<WorkflowInstanceRecord> {
    return this.commandBus.execute(new CancelWorkflowCommand(params));
  }

  getInstance(instanceId: string): Promise<WorkflowInstanceRecord> {
    return this.queryBus.execute(new GetWorkflowInstanceQuery(instanceId));
  }

  completeUserTask(params: CompleteUserTaskParams): Promise<WorkflowInstanceRecord> {
    return this.commandBus.execute(new CompleteUserTaskCommand(params));
  }

  claimTask(params: {
    taskId: string;
    userId: string;
    userPermissions: string[];
  }): Promise<WorkflowInstanceRecord> {
    return this.commandBus.execute(new ClaimTaskCommand(params));
  }

  delegateTask(params: {
    taskId: string;
    fromUserId: string;
    toUserId: string;
    userPermissions: string[];
  }): Promise<WorkflowInstanceRecord> {
    return this.commandBus.execute(new DelegateTaskCommand(params));
  }

  getMyInbox(userId: string, userPermissions: string[] = []): Promise<WorkflowInboxTaskRecord[]> {
    return this.queryBus.execute(new GetMyInboxQuery(userId, userPermissions));
  }

  getTimeline(
    instanceId: string,
    options?: { fromSequence?: number; limit?: number },
  ): Promise<WorkflowEventLogEntry[]> {
    return this.queryBus.execute(new GetWorkflowTimelineQuery(instanceId, options));
  }

  releaseInboxTasksForDeletedUser(userId: string): Promise<number> {
    return this.commandBus.execute(new ReleaseUserInboxTasksCommand(userId));
  }
}
