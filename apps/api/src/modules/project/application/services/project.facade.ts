import { Injectable } from '@nestjs/common';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { ActivityDetailDto, ActivityDetailFilterDto } from '../dtos/activity.dto';
import { GoalDetailDto } from '../dtos/goal.dto';
import { ProjectDetailDto, ProjectDetailFilterDto } from '../dtos/project.dto';
import { ListActivitiesQuery } from '../queries/list-activities/list-activities.query';
import { ListGoalsQuery } from '../queries/list-goals/list-goals.query';
import { ListProjectsQuery } from '../queries/list-projects/list-projects.query';
import { BeneficiaryDetailFilterDto } from '../dtos/beneficiary.dto';
import { CountBeneficiariesQuery } from '../queries/count-beneficiaries/count-beneficiaries.query';
import {
  AddTeamMemberCommand,
  CompleteMilestoneCommand,
  CreateMilestoneCommand,
  CreateProjectRiskCommand,
  DeactivateTeamMemberCommand,
  ListMilestonesQuery,
  ListProjectRisksQuery,
  ListTeamMembersQuery,
  ResolveProjectRiskCommand,
  UpdateMilestoneCommand,
  UpdateProjectRiskCommand,
  UpdateTeamMemberCommand,
} from '../project-subresource.commands';
import { TeamMemberRole } from '../../domain/enums/team-member.enum';
import {
  RiskCategory,
  RiskProbability,
  RiskSeverity,
  RiskStatus,
} from '../../domain/enums/risk.enum';
import { MilestoneImportance } from '../../domain/enums/milestone.enum';

@Injectable()
export class ProjectFacade {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) {}

  listProjects(
    filter: ProjectDetailFilterDto = {},
  ): Promise<PagedResponse<ProjectDetailDto>> {
    return this.queryBus.execute(new ListProjectsQuery(filter));
  }

  countBeneficiaries(
    projectFilter: ProjectDetailFilterDto = {},
    beneficiaryFilter: BeneficiaryDetailFilterDto = {},
  ): Promise<number> {
    return this.queryBus.execute(new CountBeneficiariesQuery(projectFilter, beneficiaryFilter));
  }

  listProjectGoals(params: {
    projectId: string;
    pageIndex?: number;
    pageSize?: number;
  }): Promise<PagedResponse<GoalDetailDto>> {
    return this.queryBus.execute(
      new ListGoalsQuery(params.projectId, {
        pageIndex: params.pageIndex ?? 0,
        pageSize: params.pageSize ?? 100,
      }),
    );
  }

  listActivities(
    filter: ActivityDetailFilterDto = {},
  ): Promise<PagedResponse<ActivityDetailDto>> {
    return this.queryBus.execute(new ListActivitiesQuery(filter));
  }

  listTeamMembers(projectId: string) {
    return this.queryBus.execute(new ListTeamMembersQuery(projectId));
  }

  addTeamMember(
    projectId: string,
    dto: {
      userId: string;
      role: TeamMemberRole;
      startDate: Date;
      responsibilities?: string;
      hoursAllocated?: number;
    },
  ) {
    return this.commandBus.execute(new AddTeamMemberCommand({ projectId, ...dto }));
  }

  updateTeamMember(
    id: string,
    dto: { role?: TeamMemberRole; responsibilities?: string; hoursAllocated?: number },
  ) {
    return this.commandBus.execute(new UpdateTeamMemberCommand({ id, ...dto }));
  }

  deactivateTeamMember(id: string) {
    return this.commandBus.execute(new DeactivateTeamMemberCommand(id));
  }

  listProjectRisks(projectId: string) {
    return this.queryBus.execute(new ListProjectRisksQuery(projectId));
  }

  createProjectRisk(
    projectId: string,
    dto: {
      title: string;
      category: RiskCategory;
      severity: RiskSeverity;
      probability: RiskProbability;
      identifiedDate: Date;
      description?: string;
      impact?: string;
      mitigationPlan?: string;
      ownerId?: string;
    },
  ) {
    return this.commandBus.execute(new CreateProjectRiskCommand({ projectId, ...dto }));
  }

  updateProjectRisk(
    id: string,
    dto: {
      title?: string;
      severity?: RiskSeverity;
      probability?: RiskProbability;
      mitigationPlan?: string;
      status?: RiskStatus;
    },
  ) {
    return this.commandBus.execute(new UpdateProjectRiskCommand({ id, ...dto }));
  }

  resolveProjectRisk(id: string) {
    return this.commandBus.execute(new ResolveProjectRiskCommand(id));
  }

  listMilestones(projectId: string) {
    return this.queryBus.execute(new ListMilestonesQuery(projectId));
  }

  createMilestone(
    projectId: string,
    dto: {
      name: string;
      targetDate: Date;
      importance: MilestoneImportance;
      description?: string;
    },
  ) {
    return this.commandBus.execute(new CreateMilestoneCommand({ projectId, ...dto }));
  }

  updateMilestone(
    id: string,
    dto: {
      name?: string;
      targetDate?: Date;
      importance?: MilestoneImportance;
      description?: string;
    },
  ) {
    return this.commandBus.execute(new UpdateMilestoneCommand({ id, ...dto }));
  }

  completeMilestone(id: string) {
    return this.commandBus.execute(new CompleteMilestoneCommand(id));
  }
}
