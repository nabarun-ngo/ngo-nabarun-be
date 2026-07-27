import { Inject, Injectable, NotFoundException } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { IReportDefinitionsPort } from '../../../domain/ports/report-definitions.port';
import { IReportRepository } from '../../../domain/repositories/report.repository';
import { ReportDetailDto, ReportMapper } from '../../dtos/report.dto';
import { ReportGenerationService } from '../../services/report-generation.service';
import { RegenerateReportCommand } from './regenerate-report.command';

@CommandHandler(RegenerateReportCommand)
@Injectable()
export class RegenerateReportHandler
  implements ICommandHandler<RegenerateReportCommand, ReportDetailDto>
{
  constructor(
    @Inject(IReportRepository) private readonly reportRepository: IReportRepository,
    @Inject(IReportDefinitionsPort) private readonly definitionsPort: IReportDefinitionsPort,
    private readonly reportGenerationService: ReportGenerationService,
  ) {}

  async execute(command: RegenerateReportCommand): Promise<ReportDetailDto> {
    const existing = await this.reportRepository.findById(command.params.reportId);
    if (!existing) {
      throw new NotFoundException('Report not found');
    }
    await this.definitionsPort.getDefinition(existing.reportCode);
    const report = await this.reportGenerationService.generateForReport({
      reportId: command.params.reportId,
      reportCode: existing.reportCode,
      parameters: existing.parameters ?? {},
      requestedById: command.params.requestedById,
      userPermissions: command.params.userPermissions,
      needApproval: existing.needApproval,
      approverRoles: existing.approverRoles,
      viewerRoles: existing.viewerRoles,
      reportName: existing.reportName,
      workflowId: existing.workflowId,
      regenerate: true,
    });
    return ReportMapper.toDetailDto(report);
  }
}
