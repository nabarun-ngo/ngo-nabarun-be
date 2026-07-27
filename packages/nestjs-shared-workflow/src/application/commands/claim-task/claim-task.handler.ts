import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { Injectable } from '@nestjs/common';
import { ClaimTaskCommand } from './claim-task.command';
import { WorkflowOrchestratorService } from '../../services/workflow-orchestrator.service';
import type { WorkflowInstanceRecord } from '../../../domain/ports/workflow-instance.repository';

@CommandHandler(ClaimTaskCommand)
@Injectable()
export class ClaimTaskHandler implements ICommandHandler<ClaimTaskCommand, WorkflowInstanceRecord> {
  constructor(private readonly orchestrator: WorkflowOrchestratorService) {}

  execute(command: ClaimTaskCommand): Promise<WorkflowInstanceRecord> {
    return this.orchestrator.claimTask(command.params);
  }
}
