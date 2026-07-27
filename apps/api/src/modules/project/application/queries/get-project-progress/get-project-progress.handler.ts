import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { RiskStatus } from '../../../domain/enums/risk.enum';
import { IActivityRepository } from '../../../domain/repositories/activity.repository';
import { IBeneficiaryRepository } from '../../../domain/repositories/beneficiary.repository';
import { IGoalRepository } from '../../../domain/repositories/goal.repository';
import { IMilestoneRepository } from '../../../domain/repositories/milestone.repository';
import { IProjectRiskRepository } from '../../../domain/repositories/project-risk.repository';
import { IProjectRepository } from '../../../domain/repositories/project.repository';
import { GetProjectProgressQuery } from './get-project-progress.query';

export interface ProjectProgressDto {
  projectId: string;
  budgetUtilization: number;
  spentAmount: number;
  budget: number;
  beneficiaryCount: number;
  activityCount: number;
  goalCount: number;
  milestoneCount: number;
  openRiskCount: number;
}

@QueryHandler(GetProjectProgressQuery)
@Injectable()
export class GetProjectProgressHandler implements IQueryHandler<GetProjectProgressQuery, ProjectProgressDto> {
  constructor(
    @Inject(IProjectRepository) private readonly projectRepository: IProjectRepository,
    @Inject(IActivityRepository) private readonly activityRepository: IActivityRepository,
    @Inject(IBeneficiaryRepository) private readonly beneficiaryRepository: IBeneficiaryRepository,
    @Inject(IGoalRepository) private readonly goalRepository: IGoalRepository,
    @Inject(IMilestoneRepository) private readonly milestoneRepository: IMilestoneRepository,
    @Inject(IProjectRiskRepository) private readonly projectRiskRepository: IProjectRiskRepository,
  ) {}

  async execute(q: GetProjectProgressQuery): Promise<ProjectProgressDto> {
    const project = await this.projectRepository.findById(q.projectId);
    if (!project) throw new BusinessException('Project not found');

    const [activityCount, beneficiaryCount, goalCount, milestoneCount, openRiskCount] = await Promise.all([
      this.activityRepository.count({ projectId: q.projectId }),
      this.beneficiaryRepository.countByProject(q.projectId),
      this.goalRepository.count({ projectId: q.projectId }),
      this.milestoneRepository.count({ projectId: q.projectId }),
      this.projectRiskRepository.count({ projectId: q.projectId, excludeStatus: RiskStatus.CLOSED }),
    ]);

    return {
      projectId: q.projectId,
      budgetUtilization: project.getBudgetUtilization(),
      spentAmount: project.spentAmount,
      budget: project.budget,
      beneficiaryCount,
      activityCount,
      goalCount,
      milestoneCount,
      openRiskCount,
    };
  }
}
