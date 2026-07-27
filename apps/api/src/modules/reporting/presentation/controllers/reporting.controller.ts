import {
  Body,
  Controller,
  Delete,
  Get,
  HttpCode,
  HttpStatus,
  Param,
  Post,
  Query,
  UseGuards,
} from '@nestjs/common';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { CurrentUser, RequirePermissions, UnifiedAuthGuard, requireUserId } from '@nabarun-ngo/nestjs-shared-auth';
import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';
import { DeleteReportCommand } from '../../application/commands/delete-report/delete-report.command';
import { RegenerateReportCommand } from '../../application/commands/regenerate-report/regenerate-report.command';
import { StartReportGenerationCommand } from '../../application/commands/start-report-generation/start-report-generation.command';
import {
  ReportCategoryDto,
  ReportDetailDto,
  ReportFilterDto,
  ReportInputFieldDto,
} from '../../application/dtos/report.dto';
import { GetRegisteredReportsQuery } from '../../application/queries/get-registered-reports/get-registered-reports.query';
import { GetReportInputsQuery } from '../../application/queries/get-report-inputs/get-report-inputs.query';
import { ListReportsByCodeQuery } from '../../application/queries/list-reports-by-code/list-reports-by-code.query';
import type { ListReportsByCodeResult } from '../../application/queries/list-reports-by-code/list-reports-by-code.handler';

@ApiTags('Report')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('report')
export class ReportingController {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) { }

  @Get('registered-reports')
  @RequirePermissions('read:reports')
  getRegisteredReports(): Promise<ReportCategoryDto[]> {
    return this.queryBus.execute(new GetRegisteredReportsQuery());
  }

  @Post('generate/:reportCode')
  @RequirePermissions('create:reports')
  @HttpCode(HttpStatus.ACCEPTED)
  generateReport(
    @Param('reportCode') reportCode: string,
    @Body() params: Record<string, unknown>,
    @CurrentUser() user: AuthUser,
  ): Promise<{ workflowId: string }> {
    return this.commandBus.execute(
      new StartReportGenerationCommand({
        reportCode,
        parameters: params,
        requestedById: requireUserId(user),
        userPermissions: user.permissions ?? [],
        userRoles: user.userRoles ?? [],
      }),
    );
  }

  @Get('list/:reportCode')
  @RequirePermissions('read:reports')
  listReports(
    @Param('reportCode') reportCode: string,
    @Query('pageIndex') pageIndex?: number,
    @Query('pageSize') pageSize?: number,
    @Query() filter?: ReportFilterDto,
  ): Promise<ListReportsByCodeResult> {
    return this.queryBus.execute(
      new ListReportsByCodeQuery(
        reportCode,
        pageIndex ? Number(pageIndex) : 0,
        pageSize ? Number(pageSize) : 20,
        filter,
      ),
    );
  }

  @Post(':reportId/regenerate')
  @HttpCode(HttpStatus.OK)
  regenerateReport(
    @Param('reportId') reportId: string,
    @CurrentUser() user: AuthUser,
  ): Promise<ReportDetailDto> {
    return this.commandBus.execute(
      new RegenerateReportCommand({
        reportId,
        requestedById: requireUserId(user),
        userPermissions: user.permissions ?? ['create:reports'],
      }),
    );
  }

  @Delete(':reportId')
  @RequirePermissions('delete:reports')
  @HttpCode(HttpStatus.NO_CONTENT)
  deleteReport(
    @Param('reportId') reportId: string,
    @CurrentUser() user: AuthUser,
  ): Promise<void> {
    return this.commandBus.execute(
      new DeleteReportCommand(
        reportId,
        requireUserId(user),
        user.permissions ?? ['delete:reports'],
      ),
    );
  }

  @Get('static/reportInputs')
  @RequirePermissions('read:reports')
  getReportInputs(@Query('reportCode') reportCode: string): Promise<ReportInputFieldDto[]> {
    return this.queryBus.execute(new GetReportInputsQuery(reportCode));
  }
}
