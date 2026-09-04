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
import {
  ApiAcceptedResponse,
  ApiBearerAuth,
  ApiBody,
  ApiExtraModels,
  ApiQuery,
  ApiSecurity,
  ApiTags,
} from '@nestjs/swagger';
import { CurrentUser, RequirePermissions, UnifiedAuthGuard, requireUserId } from '@nabarun-ngo/nestjs-shared-auth';
import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';
import {
  ApiAutoPagedResponse,
  ApiAutoResponse,
  ApiAutoVoidResponse,
  ApiKeyParam,
  ApiUuidParam,
  PagedResponse,
  createSuccessResponseType,
} from '@nabarun-ngo/nestjs-shared-core';
import { ApproveReportCommand } from '../../application/commands/approve-report/approve-report.command';
import { DeleteReportCommand } from '../../application/commands/delete-report/delete-report.command';
import { RegenerateReportCommand } from '../../application/commands/regenerate-report/regenerate-report.command';
import { StartReportGenerationCommand } from '../../application/commands/start-report-generation/start-report-generation.command';
import {
  ReportCategoryDto,
  ReportDetailDto,
  ReportFilterDto,
  ReportGenerationStartedDto,
  ReportInputFieldDto,
} from '../../application/dtos/report.dto';
import { GetRegisteredReportsQuery } from '../../application/queries/get-registered-reports/get-registered-reports.query';
import { GetReportInputsQuery } from '../../application/queries/get-report-inputs/get-report-inputs.query';
import { ListReportsByCodeQuery } from '../../application/queries/list-reports-by-code/list-reports-by-code.query';

const EXAMPLE_REPORT_CODE = 'DONATION_SUMMARY_REPORT';

/**
 * Generation is asynchronous, so the 202 is described explicitly — `ApiAutoResponse`
 * would file the schema under 200 while the route keeps answering 202.
 */
const SuccessResponseReportGenerationStarted = createSuccessResponseType(ReportGenerationStartedDto);

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
  @ApiAutoResponse(ReportCategoryDto, {
    isArray: true,
    description: 'Active report definitions the caller can generate',
  })
  getRegisteredReports(): Promise<ReportCategoryDto[]> {
    return this.queryBus.execute(new GetRegisteredReportsQuery());
  }

  @Post('generate/:reportCode')
  @RequirePermissions('create:reports')
  @HttpCode(HttpStatus.ACCEPTED)
  @ApiKeyParam('reportCode', EXAMPLE_REPORT_CODE, 'Code of the registered report definition')
  @ApiBody({
    required: false,
    description: 'Values for the input fields the report definition declares',
    schema: {
      type: 'object',
      additionalProperties: true,
      example: { startDate: '2026-03-01T00:00:00.000Z', endDate: '2026-03-31T23:59:59.000Z' },
    },
  })
  @ApiExtraModels(ReportGenerationStartedDto)
  @ApiAcceptedResponse({
    description: 'Report generation workflow started',
    type: SuccessResponseReportGenerationStarted,
  })
  generateReport(
    @Param('reportCode') reportCode: string,
    @Body() params: Record<string, unknown>,
    @CurrentUser() user: AuthUser,
  ): Promise<ReportGenerationStartedDto> {
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
  @ApiKeyParam('reportCode', EXAMPLE_REPORT_CODE, 'Code of the registered report definition')
  @ApiAutoPagedResponse(ReportDetailDto, { description: 'Page of generated reports' })
  listReports(
    @Param('reportCode') reportCode: string,
    @Query() filter?: ReportFilterDto,
  ): Promise<PagedResponse<ReportDetailDto>> {
    return this.queryBus.execute(new ListReportsByCodeQuery(reportCode, filter));
  }

  @Post(':reportId/regenerate')
  @HttpCode(HttpStatus.OK)
  @ApiUuidParam('reportId', 'Identifier of the generated report')
  @ApiAutoResponse(ReportDetailDto, { description: 'Report after the new version was generated' })
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

  @Post(':reportId/approve')
  @RequirePermissions('approve:reports')
  @HttpCode(HttpStatus.OK)
  @ApiUuidParam('reportId', 'Identifier of the generated report')
  @ApiAutoResponse(ReportDetailDto, { description: 'Report after it was approved' })
  approveReport(
    @Param('reportId') reportId: string,
    @CurrentUser() user: AuthUser,
  ): Promise<ReportDetailDto> {
    return this.commandBus.execute(
      new ApproveReportCommand({
        reportId,
        approvedById: requireUserId(user),
        userPermissions: user.permissions ?? [],
      }),
    );
  }

  @Delete(':reportId')
  @RequirePermissions('delete:reports')
  @ApiUuidParam('reportId', 'Identifier of the generated report')
  @ApiAutoVoidResponse({ status: HttpStatus.NO_CONTENT, description: 'Report deleted' })
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
  @ApiQuery({
    name: 'reportCode',
    type: String,
    required: true,
    example: EXAMPLE_REPORT_CODE,
    description: 'Code of the registered report definition',
  })
  @ApiAutoResponse(ReportInputFieldDto, {
    isArray: true,
    description: 'Input fields the report generation request expects',
  })
  getReportInputs(@Query('reportCode') reportCode: string): Promise<ReportInputFieldDto[]> {
    return this.queryBus.execute(new GetReportInputsQuery(reportCode));
  }
}
