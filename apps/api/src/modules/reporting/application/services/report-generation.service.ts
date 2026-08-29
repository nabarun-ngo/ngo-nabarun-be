import { Inject, Injectable, Logger, NotFoundException } from '@nestjs/common';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { Report } from '../../domain/aggregates/report/report.aggregate';
import { ReportStatus } from '../../domain/enums/report-status.enum';
import { IReportRepository } from '../../domain/repositories/report.repository';
import { IReportDefinitionsPort } from '../../domain/ports/report-definitions.port';
import { ReportRegistryService } from './report-registry.service';
import { ReportingDmsFacade } from '../../infrastructure/adapters/reporting-dms.facade';

@Injectable()
export class ReportGenerationService {
  private readonly logger = new Logger(ReportGenerationService.name);

  constructor(
    private readonly registry: ReportRegistryService,
    @Inject(IReportRepository) private readonly reportRepository: IReportRepository,
    @Inject(IReportDefinitionsPort) private readonly definitionsPort: IReportDefinitionsPort,
    private readonly dmsFacade: ReportingDmsFacade,
  ) { }

  /**
   * Workflow engine is disconnected: generate the report directly (no workflow start).
   */
  async startReportWorkflow(params: {
    reportCode: string;
    parameters: Record<string, unknown>;
    requestedById: string;
    userPermissions: string[];
    userRoles: string[];
  }): Promise<{ workflowId: string; reportId?: string }> {
    const definition = await this.definitionsPort.getDefinition(params.reportCode);
    if (!definition || !definition.isActive) {
      this.logger.warn(`Report definition not found or inactive: ${params.reportCode}`);
      throw new NotFoundException('Report definition not found');
    }
    if (definition.approverRoles?.length) {
      const allowed = params.userRoles.some((role) => definition.approverRoles?.includes(role));
      if (!allowed) {
        this.logger.warn(
          `Report generation denied for ${params.reportCode}; requiredRoles=${definition.approverRoles.join(',')}`,
        );
        throw new BusinessException(
          `You do not have the required role to generate this report. Required: ${definition.approverRoles.join(', ')}`,
          'REPORT_GENERATION_FORBIDDEN',
          403,
          'You do not have permission to generate this report.',
        );
      }
    }

    const report = await this.generateForReport({
      reportCode: params.reportCode,
      parameters: params.parameters,
      requestedById: params.requestedById,
      userPermissions: params.userPermissions,
      needApproval: Boolean(definition.requiresApproval),
      approverRoles: definition.approverRoles ?? [],
      viewerRoles: definition.visibleToRoles ?? [],
      reportName: definition.displayName,
    });

    return { workflowId: '', reportId: report.id };
  }

  async generateForReport(params: {
    reportId?: string;
    reportCode: string;
    parameters: Record<string, unknown>;
    requestedById: string;
    userPermissions: string[];
    needApproval: boolean;
    approverRoles: string[];
    viewerRoles: string[];
    reportName: string;
    workflowId?: string;
    regenerate?: boolean;
  }): Promise<Report> {
    const provider = this.registry.getProvider(params.reportCode);
    if (!provider) {
      this.logger.warn(`Report provider not found: ${params.reportCode}`);
      throw new NotFoundException('Report provider not found');
    }

    let report: Report;
    if (params.regenerate && params.reportId) {
      report = (await this.reportRepository.findById(params.reportId))!;
      if (!report) throw new NotFoundException('Report not found');
      if (report.status === ReportStatus.APPROVED) {
        throw new BusinessException('Report is already approved');
      }
    } else if (params.reportId) {
      report = (await this.reportRepository.findById(params.reportId))!;
      if (!report) throw new NotFoundException('Report not found');
    } else {
      report = Report.create({
        reportCode: params.reportCode,
        reportName: params.reportName,
        requestedById: params.requestedById,
        parameters: params.parameters,
        needApproval: params.needApproval,
        approverRoles: params.approverRoles,
        viewerRoles: params.viewerRoles,
      });
      report = await this.reportRepository.create(report);
    }

    const generated = await provider.generate(params.parameters as never);
    report.incrementVersion();

    const docId = await this.dmsFacade.uploadReportDocument({
      buffer: generated.buffer,
      fileName: `${generated.fileName}-v${report.docVersion}.${generated.fileExtension}`,
      contentType: generated.contentType,
      reportId: report.id,
      userId: params.requestedById,
      userPermissions: params.userPermissions,
    });
    report.docId = docId;

    if (params.workflowId) {
      report.workflowId = params.workflowId;
    }

    if (!params.needApproval && !params.regenerate) {
      report.markApproved(params.requestedById);
    }

    const updated = await this.reportRepository.update(report.id, report);
    return updated;
  }

  async finalizeApproval(reportId: string, approvedById: string): Promise<Report> {
    const report = await this.reportRepository.findById(reportId);
    if (!report) throw new NotFoundException('Report not found');
    report.markApproved(approvedById);
    return this.reportRepository.update(report.id, report);
  }

  /**
   * Approval on request from a reviewer. Workflow review is disconnected —
   * reports are finalized directly.
   */
  async approveReport(params: {
    reportId: string;
    approvedById: string;
    userPermissions: string[];
  }): Promise<Report> {
    const report = await this.reportRepository.findById(params.reportId);
    if (!report) throw new NotFoundException('Report not found');
    if (report.status === ReportStatus.APPROVED) {
      throw new BusinessException('Report is already approved');
    }

    return this.finalizeApproval(params.reportId, params.approvedById);
  }

  async deleteReport(
    reportId: string,
    userId: string,
    userPermissions: string[],
  ): Promise<void> {
    const report = await this.reportRepository.findById(reportId);
    if (!report) throw new NotFoundException('Report not found');
    const docs = await this.dmsFacade.getDocuments(reportId, userId, userPermissions);
    if (docs.length) {
      await this.dmsFacade.deleteDocuments(docs.map((d) => d.id), userId, userPermissions);
    }
    await this.reportRepository.delete(reportId);
  }
}
