import { Injectable, NotFoundException } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { ReportInputFieldDto } from '../../dtos/report.dto';
import { ReportRegistryService } from '../../services/report-registry.service';
import { GetReportInputsQuery } from './get-report-inputs.query';

@QueryHandler(GetReportInputsQuery)
@Injectable()
export class GetReportInputsHandler implements IQueryHandler<GetReportInputsQuery, ReportInputFieldDto[]> {
  constructor(private readonly registry: ReportRegistryService) {}

  execute(query: GetReportInputsQuery): Promise<ReportInputFieldDto[]> {
    const provider = this.registry.getProvider(query.reportCode);
    if (!provider) {
      throw new NotFoundException(`Report provider for ${query.reportCode} not found`);
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
