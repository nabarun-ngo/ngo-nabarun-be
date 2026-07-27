import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IReportRepository } from '../../../domain/repositories/report.repository';
import { ReportDetailDto, ReportMapper } from '../../dtos/report.dto';
import { ListReportsByCodeQuery } from './list-reports-by-code.query';

export type ListReportsByCodeResult = {
  content: ReportDetailDto[];
  totalSize: number;
  pageIndex: number;
  pageSize: number;
};

@QueryHandler(ListReportsByCodeQuery)
@Injectable()
export class ListReportsByCodeHandler
  implements IQueryHandler<ListReportsByCodeQuery, ListReportsByCodeResult>
{
  constructor(@Inject(IReportRepository) private readonly reportRepository: IReportRepository) {}

  async execute(query: ListReportsByCodeQuery): Promise<ListReportsByCodeResult> {
    const page = await this.reportRepository.findPaged({
      pageIndex: query.pageIndex,
      pageSize: query.pageSize,
      props: {
        reportCode: query.reportCode,
        status: query.filter?.status ? [query.filter.status] : undefined,
        requestedById: query.filter?.requestedById,
      },
    });
    return {
      content: page.content.map(ReportMapper.toDetailDto),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
