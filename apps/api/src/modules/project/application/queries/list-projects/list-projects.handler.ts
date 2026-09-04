import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { IProjectRepository } from '../../../domain/repositories/project.repository';
import { ProjectDetailDto } from '../../dtos/project.dto';
import { ProjectMapper } from '../../mappers/project.mapper';
import { ListProjectsQuery } from './list-projects.query';

@QueryHandler(ListProjectsQuery)
@Injectable()
export class ListProjectsHandler implements IQueryHandler<ListProjectsQuery, PagedResponse<ProjectDetailDto>> {
  constructor(@Inject(IProjectRepository) private readonly repo: IProjectRepository) { }

  async execute(query: ListProjectsQuery): Promise<PagedResponse<ProjectDetailDto>> {
    const { pageIndex, pageSize, sortBy, sortDir, ...props } = query.filter ?? {};
    const filter = new BaseFilter(props, pageIndex ?? 0, pageSize ?? 20, sortBy, sortDir);
    const page = await this.repo.findPaged({
      pageIndex: filter.pageIndex,
      pageSize: filter.pageSize,
      props: filter.props,
    });
    return {
      content: page.content.map(ProjectMapper.toDto),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
