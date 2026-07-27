import { DynamicModule, Module, ModuleMetadata } from '@nestjs/common';
import { CqrsModule } from '@nestjs/cqrs';
import { DiscoveryModule } from '@nestjs/core';
import { IReportRepository } from './domain/repositories/report.repository';
import { IReportDefinitionsPort } from './domain/ports/report-definitions.port';
import { ReportPrismaRepository } from '../../shared/persistence/reporting/report.prisma-repository';
import { ReportDefinitionsAdapter } from './infrastructure/adapters/report-definitions.adapter';
import { ReportingDmsFacade } from './infrastructure/adapters/reporting-dms.facade';
import { ReportRegistryService } from './application/services/report-registry.service';
import { ReportGenerationService } from './application/services/report-generation.service';
import { ReportingFacade } from './application/services/reporting.facade';
import { ReportingController } from './presentation/controllers/reporting.controller';
import { GenerateReportHandler } from './application/handlers/workflow/generate-report.handler';
import { RegenerateReportHandler as RegenerateReportWorkflowHandler } from './application/handlers/workflow/regenerate-report.handler';
import { FinalizeReportApprovalHandler } from './application/handlers/workflow/finalize-report-approval.handler';
import { TriggerReportGenerationHandler } from './application/handlers/queue/trigger-report-generation.handler';
import { GetRegisteredReportsHandler } from './application/queries/get-registered-reports/get-registered-reports.handler';
import { ListReportsByCodeHandler } from './application/queries/list-reports-by-code/list-reports-by-code.handler';
import { GetReportInputsHandler } from './application/queries/get-report-inputs/get-report-inputs.handler';
import { StartReportGenerationHandler } from './application/commands/start-report-generation/start-report-generation.handler';
import { RegenerateReportHandler } from './application/commands/regenerate-report/regenerate-report.handler';
import { DeleteReportHandler } from './application/commands/delete-report/delete-report.handler';

const QUERY_HANDLERS = [
  GetRegisteredReportsHandler,
  ListReportsByCodeHandler,
  GetReportInputsHandler,
];

const COMMAND_HANDLERS = [
  StartReportGenerationHandler,
  RegenerateReportHandler,
  DeleteReportHandler,
];

const WORKFLOW_HANDLERS = [
  GenerateReportHandler,
  RegenerateReportWorkflowHandler,
  FinalizeReportApprovalHandler,
];

@Module({})
export class ReportingModule {
  static forRoot(options: { imports?: ModuleMetadata['imports'] } = {}): DynamicModule {
    return {
      module: ReportingModule,
      imports: [CqrsModule, DiscoveryModule, ...(options.imports ?? [])],
      controllers: [ReportingController],
      providers: [
        { provide: IReportRepository, useClass: ReportPrismaRepository },
        { provide: IReportDefinitionsPort, useClass: ReportDefinitionsAdapter },
        ReportingDmsFacade,
        ReportRegistryService,
        ReportGenerationService,
        ReportingFacade,
        TriggerReportGenerationHandler,
        ...QUERY_HANDLERS,
        ...COMMAND_HANDLERS,
        ...WORKFLOW_HANDLERS,
      ],
      exports: [ReportingFacade],
    };
  }
}
