import { Injectable, Logger, NotFoundException } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { ReportInputFieldDto } from '../../dtos/report.dto';
import { ReportRegistryService } from '../../services/report-registry.service';
import { GetReportInputsQuery } from './get-report-inputs.query';

@QueryHandler(GetReportInputsQuery)
@Injectable()
export class GetReportInputsHandler implements IQueryHandler<GetReportInputsQuery, ReportInputFieldDto[]> {
  private readonly logger = new Logger(GetReportInputsHandler.name);

  constructor(private readonly registry: ReportRegistryService) {}

  execute(query: GetReportInputsQuery): Promise<ReportInputFieldDto[]> {
    const provider = this.registry.getProvider(query.reportCode);
    if (!provider) {
      this.logger.warn(`Report provider not found: ${query.reportCode}`);
      throw new NotFoundException('Report provider not found');
    }
    return Promise.resolve(
      provider.reportParams.map((field) => ({
        key: field.key,
        label: field.label,
        fieldType: field.defKey,
        mandatory: field.mandatory,
      })),
    );
  }
}
