import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { Inject, Injectable } from '@nestjs/common';
import { StartWorkflowCommand } from './start-workflow.command';
import { WorkflowOrchestratorService } from '../../services/workflow-orchestrator.service';
import type { WorkflowInstanceRecord } from '../../../domain/ports/workflow-instance.repository';

@CommandHandler(StartWorkflowCommand)
@Injectable()
export class StartWorkflowHandler implements ICommandHandler<StartWorkflowCommand, WorkflowInstanceRecord> {
  constructor(private readonly orchestrator: WorkflowOrchestratorService) {}

  execute(command: StartWorkflowCommand): Promise<WorkflowInstanceRecord> {
    return this.orchestrator.startWorkflow(command.params);
  }
}
