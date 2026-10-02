import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { Injectable } from '@nestjs/common';
import { ReportGenerationService } from '../../services/report-generation.service';
import { StartReportGenerationCommand } from './start-report-generation.command';

@CommandHandler(StartReportGenerationCommand)
@Injectable()
export class StartReportGenerationHandler
  implements ICommandHandler<StartReportGenerationCommand, { workflowId: string }>
{
  constructor(private readonly reportGenerationService: ReportGenerationService) {}

  execute(command: StartReportGenerationCommand): Promise<{ workflowId: string }> {
    return this.reportGenerationService.startReportWorkflow(command.params);
  }
}
