import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryBus, QueryHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { IActivityRepository } from '../../../domain/repositories/activity.repository';
import { IMilestoneRepository } from '../../../domain/repositories/milestone.repository';
import { IProjectRepository } from '../../../domain/repositories/project.repository';
import { ProjectMapper } from '../../mappers/project.mapper';
import { GetProjectDashboardQuery } from './get-project-dashboard.query';
import { GetProjectProgressQuery } from '../get-project-progress/get-project-progress.query';
import { ProjectProgressDto } from '../get-project-progress/get-project-progress.handler';

export interface ProjectDashboardDto {
  project: ReturnType<typeof ProjectMapper.toDto>;
  progress: ProjectProgressDto;
  recentActivities: { id: string; name: string; status: string; scale: string }[];
  upcomingMilestones: { id: string; name: string; targetDate: Date; status: string }[];
}

@QueryHandler(GetProjectDashboardQuery)
@Injectable()
export class GetProjectDashboardHandler implements IQueryHandler<GetProjectDashboardQuery, ProjectDashboardDto> {
  constructor(
    @Inject(IProjectRepository) private readonly projectRepository: IProjectRepository,
    @Inject(IActivityRepository) private readonly activityRepository: IActivityRepository,
    @Inject(IMilestoneRepository) private readonly milestoneRepository: IMilestoneRepository,
    private readonly queryBus: QueryBus,
  ) {}

  async execute(q: GetProjectDashboardQuery): Promise<ProjectDashboardDto> {
    const project = await this.projectRepository.findById(q.projectId);
    if (!project) throw new BusinessException('Project not found');

    const [recentActivities, upcomingMilestones, progress] = await Promise.all([
      this.activityRepository.findRecentSummariesByProjectId(q.projectId, 5),
      this.milestoneRepository.findUpcomingSummariesByProjectId(q.projectId, 5),
      this.queryBus.execute<GetProjectProgressQuery, ProjectProgressDto>(
        new GetProjectProgressQuery(q.projectId),
      ),
    ]);

    return { project: ProjectMapper.toDto(project), progress, recentActivities, upcomingMilestones };
  }
}
