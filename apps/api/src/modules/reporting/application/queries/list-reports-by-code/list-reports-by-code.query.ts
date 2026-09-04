import { ReportFilterDto } from '../../dtos/report.dto';

export class ListReportsByCodeQuery {
  constructor(
    public readonly reportCode: string,
    public readonly filter: ReportFilterDto = {},
  ) {}
}
