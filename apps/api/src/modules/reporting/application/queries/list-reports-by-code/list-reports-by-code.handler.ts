import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { IReportRepository } from '../../../domain/repositories/report.repository';
import { ReportDetailDto, ReportMapper } from '../../dtos/report.dto';
import { ListReportsByCodeQuery } from './list-reports-by-code.query';

@QueryHandler(ListReportsByCodeQuery)
@Injectable()
export class ListReportsByCodeHandler
  implements IQueryHandler<ListReportsByCodeQuery, PagedResponse<ReportDetailDto>>
{
  constructor(@Inject(IReportRepository) private readonly reportRepository: IReportRepository) {}

  async execute(query: ListReportsByCodeQuery): Promise<PagedResponse<ReportDetailDto>> {
    const page = await this.reportRepository.findPaged({
      pageIndex: query.filter.pageIndex ?? 0,
      pageSize: query.filter.pageSize ?? 20,
      props: {
        reportCode: query.reportCode,
        status: query.filter.status ? [query.filter.status] : undefined,
        requestedById: query.filter.requestedById,
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
