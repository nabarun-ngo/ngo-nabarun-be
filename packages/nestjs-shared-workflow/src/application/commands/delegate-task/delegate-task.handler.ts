import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { Injectable } from '@nestjs/common';
import { DelegateTaskCommand } from './delegate-task.command';
import { WorkflowOrchestratorService } from '../../services/workflow-orchestrator.service';
import type { WorkflowInstanceRecord } from '../../../domain/ports/workflow-instance.repository';

@CommandHandler(DelegateTaskCommand)
@Injectable()
export class DelegateTaskHandler implements ICommandHandler<DelegateTaskCommand, WorkflowInstanceRecord> {
  constructor(private readonly orchestrator: WorkflowOrchestratorService) {}

  execute(command: DelegateTaskCommand): Promise<WorkflowInstanceRecord> {
    return this.orchestrator.delegateTask(command.params);
  }
}
