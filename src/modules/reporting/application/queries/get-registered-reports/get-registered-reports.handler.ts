import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IReportDefinitionsPort } from '../../../domain/ports/report-definitions.port';
import { ReportCategoryDto, ReportMapper } from '../../dtos/report.dto';
import { GetRegisteredReportsQuery } from './get-registered-reports.query';

@QueryHandler(GetRegisteredReportsQuery)
@Injectable()
export class GetRegisteredReportsHandler
  implements IQueryHandler<GetRegisteredReportsQuery, ReportCategoryDto[]>
{
  constructor(
    @Inject(IReportDefinitionsPort) private readonly definitionsPort: IReportDefinitionsPort,
  ) {}

  async execute(): Promise<ReportCategoryDto[]> {
    const definitions = await this.definitionsPort.listDefinitions();
    return definitions.filter((d) => d.isActive).map(ReportMapper.toCategoryDto);
  }
}
