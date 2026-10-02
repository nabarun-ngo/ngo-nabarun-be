import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { Injectable } from '@nestjs/common';
import { ReportGenerationService } from '../../services/report-generation.service';
import { DeleteReportCommand } from './delete-report.command';

@CommandHandler(DeleteReportCommand)
@Injectable()
export class DeleteReportHandler implements ICommandHandler<DeleteReportCommand, void> {
  constructor(private readonly reportGenerationService: ReportGenerationService) {}

  execute(command: DeleteReportCommand): Promise<void> {
    return this.reportGenerationService.deleteReport(
      command.reportId,
      command.userId,
      command.userPermissions,
    );
  }
}
