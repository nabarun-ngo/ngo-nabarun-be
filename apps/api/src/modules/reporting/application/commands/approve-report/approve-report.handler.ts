import { Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { ReportDetailDto, ReportMapper } from '../../dtos/report.dto';
import { ReportGenerationService } from '../../services/report-generation.service';
import { ApproveReportCommand } from './approve-report.command';

@CommandHandler(ApproveReportCommand)
@Injectable()
export class ApproveReportHandler
  implements ICommandHandler<ApproveReportCommand, ReportDetailDto>
{
  constructor(private readonly reportGenerationService: ReportGenerationService) {}

  async execute(command: ApproveReportCommand): Promise<ReportDetailDto> {
    const report = await this.reportGenerationService.approveReport(command.params);
    return ReportMapper.toDetailDto(report);
  }
}
