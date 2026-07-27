import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { Injectable } from '@nestjs/common';
import { CancelWorkflowCommand } from './cancel-workflow.command';
import { WorkflowOrchestratorService } from '../../services/workflow-orchestrator.service';
import type { WorkflowInstanceRecord } from '../../../domain/ports/workflow-instance.repository';

@CommandHandler(CancelWorkflowCommand)
@Injectable()
export class CancelWorkflowHandler
  implements ICommandHandler<CancelWorkflowCommand, WorkflowInstanceRecord>
{
  constructor(private readonly orchestrator: WorkflowOrchestratorService) {}

  execute(command: CancelWorkflowCommand): Promise<WorkflowInstanceRecord> {
    return this.orchestrator.cancelWorkflow(command.params);
  }
}
