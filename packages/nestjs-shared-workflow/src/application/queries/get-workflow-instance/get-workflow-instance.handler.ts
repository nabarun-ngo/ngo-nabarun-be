import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { Injectable } from '@nestjs/common';
import { GetWorkflowInstanceQuery } from './get-workflow-instance.query';
import { WorkflowOrchestratorService } from '../../services/workflow-orchestrator.service';
import type { WorkflowInstanceRecord } from '../../../domain/ports/workflow-instance.repository';

@QueryHandler(GetWorkflowInstanceQuery)
@Injectable()
export class GetWorkflowInstanceHandler
  implements IQueryHandler<GetWorkflowInstanceQuery, WorkflowInstanceRecord>
{
  constructor(private readonly orchestrator: WorkflowOrchestratorService) {}

  execute(query: GetWorkflowInstanceQuery): Promise<WorkflowInstanceRecord> {
    return this.orchestrator.getInstance(query.instanceId);
  }
}
