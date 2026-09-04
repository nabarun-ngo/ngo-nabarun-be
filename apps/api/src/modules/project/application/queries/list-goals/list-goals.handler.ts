import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { IGoalRepository } from '../../../domain/repositories/goal.repository';
import { GoalMapper } from '../../mappers/goal.mapper';
import { GoalDetailDto } from '../../dtos/goal.dto';
import { ListGoalsQuery } from './list-goals.query';

@QueryHandler(ListGoalsQuery)
@Injectable()
export class ListGoalsHandler implements IQueryHandler<ListGoalsQuery, PagedResponse<GoalDetailDto>> {
  constructor(@Inject(IGoalRepository) private readonly repo: IGoalRepository) { }

  async execute(q: ListGoalsQuery): Promise<PagedResponse<GoalDetailDto>> {
    const { pageIndex, pageSize, sortBy, sortDir } = q.filter ?? {};
    const page = await this.repo.findPaged(
      new BaseFilter({ projectId: q.projectId }, pageIndex ?? 0, pageSize ?? 20, sortBy, sortDir),
    );
    return {
      content: page.content.map(GoalMapper.toDto),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
