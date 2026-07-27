import { ReportStatus } from '../../../domain/enums/report-status.enum';

export class ListReportsByCodeQuery {
  constructor(
    public readonly reportCode: string,
    public readonly pageIndex: number,
    public readonly pageSize: number,
    public readonly filter?: {
      status?: ReportStatus;
      requestedById?: string;
    },
  ) {}
}
