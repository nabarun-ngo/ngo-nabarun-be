import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { Injectable } from '@nestjs/common';
import { CompleteUserTaskCommand } from './complete-user-task.command';
import { WorkflowOrchestratorService } from '../../services/workflow-orchestrator.service';
import type { WorkflowInstanceRecord } from '../../../domain/ports/workflow-instance.repository';

@CommandHandler(CompleteUserTaskCommand)
@Injectable()
export class CompleteUserTaskHandler
  implements ICommandHandler<CompleteUserTaskCommand, WorkflowInstanceRecord>
{
  constructor(private readonly orchestrator: WorkflowOrchestratorService) {}

  execute(command: CompleteUserTaskCommand): Promise<WorkflowInstanceRecord> {
    return this.orchestrator.completeUserTask(command.params);
  }
}
