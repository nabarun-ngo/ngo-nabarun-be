import { Injectable } from '@nestjs/common';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { DeleteReportCommand } from '../commands/delete-report/delete-report.command';
import { RegenerateReportCommand } from '../commands/regenerate-report/regenerate-report.command';
import { StartReportGenerationCommand } from '../commands/start-report-generation/start-report-generation.command';
import { GetRegisteredReportsQuery } from '../queries/get-registered-reports/get-registered-reports.query';
import { GetReportInputsQuery } from '../queries/get-report-inputs/get-report-inputs.query';
import { ListReportsByCodeQuery } from '../queries/list-reports-by-code/list-reports-by-code.query';
import type { ReportCategoryDto, ReportDetailDto, ReportInputFieldDto } from '../dtos/report.dto';
import type { ListReportsByCodeResult } from '../queries/list-reports-by-code/list-reports-by-code.handler';

@Injectable()
export class ReportingFacade {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) {}

  getRegisteredReports(): Promise<ReportCategoryDto[]> {
    return this.queryBus.execute(new GetRegisteredReportsQuery());
  }

  listReportsByCode(
    reportCode: string,
    pageIndex: number,
    pageSize: number,
  ): Promise<ListReportsByCodeResult> {
    return this.queryBus.execute(new ListReportsByCodeQuery(reportCode, pageIndex, pageSize));
  }

  getReportInputs(reportCode: string): Promise<ReportInputFieldDto[]> {
    return this.queryBus.execute(new GetReportInputsQuery(reportCode));
  }

  startReportGeneration(params: StartReportGenerationCommand['params']): Promise<{ workflowId: string }> {
    return this.commandBus.execute(new StartReportGenerationCommand(params));
  }

  regenerateReport(params: RegenerateReportCommand['params']): Promise<ReportDetailDto> {
    return this.commandBus.execute(new RegenerateReportCommand(params));
  }

  deleteReport(reportId: string, userId: string, userPermissions: string[]): Promise<void> {
    return this.commandBus.execute(new DeleteReportCommand(reportId, userId, userPermissions));
  }
}
