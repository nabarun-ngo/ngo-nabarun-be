import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { IActivityRepository } from '../../../domain/repositories/activity.repository';
import { ActivityDetailDto } from '../../dtos/activity.dto';
import { ActivityMapper } from '../../mappers/activity.mapper';
import { ListActivitiesQuery } from './list-activities.query';

@QueryHandler(ListActivitiesQuery)
@Injectable()
export class ListActivitiesHandler implements IQueryHandler<ListActivitiesQuery, PagedResponse<ActivityDetailDto>> {
  constructor(@Inject(IActivityRepository) private readonly repo: IActivityRepository) { }

  async execute(query: ListActivitiesQuery): Promise<PagedResponse<ActivityDetailDto>> {
    const { pageIndex, pageSize, sortBy, sortDir, ...props } = query.filter ?? {};
    const filter = new BaseFilter(props, pageIndex ?? 0, pageSize ?? 20, sortBy, sortDir);
    const page = await this.repo.findPaged({
      pageIndex: filter.pageIndex,
      pageSize: filter.pageSize,
      props: filter.props,
    });
    return {
      content: page.content.map(ActivityMapper.toDto),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
