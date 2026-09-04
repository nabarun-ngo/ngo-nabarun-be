import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { PaginatedQueryDto } from '@nabarun-ngo/nestjs-shared-core';
import { Report } from '../../domain/aggregates/report/report.aggregate';
import { ReportStatus } from '../../domain/enums/report-status.enum';
import { ReportDefinition } from '../../domain/reporting.interface';

export class ReportDetailDto {
  @ApiProperty({ example: 'NRP482913' })
  id: string;

  @ApiProperty({ example: 'DONATION_SUMMARY_REPORT' })
  reportCode: string;

  @ApiProperty({ example: 'Donation Summary Report' })
  reportName: string;

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  requestedById?: string;

  @ApiPropertyOptional({ example: 'Asha Verma' })
  requestedByName?: string;

  @ApiProperty({ enum: ReportStatus, example: ReportStatus.DRAFT })
  status: ReportStatus;

  @ApiPropertyOptional({
    type: 'object',
    additionalProperties: true,
    example: { startDate: '2026-03-01T00:00:00.000Z', endDate: '2026-03-31T23:59:59.000Z' },
  })
  parameters?: Record<string, unknown>;

  @ApiProperty({ example: true })
  needApproval: boolean;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  approvedById?: string;

  @ApiPropertyOptional({ example: 'Rahul Mehta' })
  approvedByName?: string;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  approvedAt?: Date;

  @ApiProperty({ type: [String], example: ['FINANCE_ADMIN'] })
  approvers: string[];

  @ApiProperty({ type: [String], example: ['FINANCE_ADMIN', 'FINANCE_VIEWER'] })
  viewers: string[];

  @ApiPropertyOptional({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  dmsDocumentId?: string;

  @ApiProperty({ example: 2 })
  version: number;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  workflowId?: string;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  createdAt: Date;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  updatedAt: Date;
}

export class ReportCategoryDto {
  @ApiProperty({ example: 'DONATION_SUMMARY_REPORT' })
  reportCode: string;

  @ApiProperty({ example: 'Donation Summary Report' })
  reportName: string;

  @ApiPropertyOptional({ example: 'Paid and pending donations for a chosen period.' })
  description?: string;

  @ApiProperty({ type: [String], example: ['FINANCE_ADMIN', 'FINANCE_VIEWER'] })
  viewerRoles: string[];

  @ApiPropertyOptional({ type: [String], example: ['FINANCE_ADMIN'] })
  manageRoles?: string[];

  @ApiPropertyOptional({ example: true })
  isActive?: boolean;
}

export class ReportFilterDto extends PaginatedQueryDto {
  @ApiPropertyOptional({ enum: ReportStatus, example: ReportStatus.DRAFT })
  status?: ReportStatus;

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  requestedById?: string;
}

export class ReportInputFieldDto {
  @ApiProperty({ example: 'startDate' })
  key: string;

  @ApiProperty({ example: 'Start Date' })
  label: string;

  @ApiProperty({ example: 'INPUT_DATE_FIELD' })
  fieldType: string;

  @ApiPropertyOptional({ example: true })
  mandatory?: boolean;
}

export class ReportGenerationStartedDto {
  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  workflowId: string;
}

export class ReportMapper {
  static toDetailDto(report: Report): ReportDetailDto {
    const requestedByName = report.requestedBy
      ? [report.requestedBy.firstName, report.requestedBy.lastName].filter(Boolean).join(' ')
      : undefined;
    const approvedByName = report.approvedBy
      ? [report.approvedBy.firstName, report.approvedBy.lastName].filter(Boolean).join(' ')
      : undefined;
    return {
      id: report.id,
      reportCode: report.reportCode,
      reportName: report.reportName,
      requestedById: report.requestedBy?.id,
      requestedByName,
      status: report.status,
      parameters: report.parameters,
      needApproval: report.needApproval,
      approvedById: report.approvedBy?.id,
      approvedByName,
      approvedAt: report.approvedAt,
      approvers: report.approverRoles,
      viewers: report.viewerRoles,
      dmsDocumentId: report.docId,
      version: report.docVersion,
      workflowId: report.workflowId,
      createdAt: report.createdAt!,
      updatedAt: report.updatedAt!,
    };
  }

  static toCategoryDto(definition: ReportDefinition): ReportCategoryDto {
    return {
      reportCode: definition.reportCode,
      reportName: definition.displayName,
      description: definition.description,
      viewerRoles: definition.visibleToRoles,
      manageRoles: definition.approverRoles,
      isActive: definition.isActive,
    };
  }
}
