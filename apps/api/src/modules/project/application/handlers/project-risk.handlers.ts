import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler, QueryHandler, IQueryHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import {
  RiskCategory,
  RiskProbability,
  RiskSeverity,
  RiskStatus,
} from '../../domain/enums/risk.enum';
import { IProjectRepository } from '../../domain/repositories/project.repository';
import {
  IProjectRiskRepository,
  ProjectRiskRecord,
} from '../../domain/repositories/project-risk.repository';
import {
  CreateProjectRiskCommand,
  ListProjectRisksQuery,
  ResolveProjectRiskCommand,
  UpdateProjectRiskCommand,
} from '../project-subresource.commands';

@QueryHandler(ListProjectRisksQuery)
@Injectable()
export class ListProjectRisksHandler implements IQueryHandler<ListProjectRisksQuery> {
  constructor(@Inject(IProjectRiskRepository) private readonly repo: IProjectRiskRepository) {}

  async execute(query: ListProjectRisksQuery) {
    const items = await this.repo.findByProjectId(query.projectId);
    return { items, total: items.length };
  }
}

@CommandHandler(CreateProjectRiskCommand)
@Injectable()
export class CreateProjectRiskHandler
  implements ICommandHandler<CreateProjectRiskCommand, ProjectRiskRecord>
{
  constructor(
    @Inject(IProjectRiskRepository) private readonly repo: IProjectRiskRepository,
    @Inject(IProjectRepository) private readonly projectRepo: IProjectRepository,
  ) {}

  async execute({ params }: CreateProjectRiskCommand): Promise<ProjectRiskRecord> {
    const projectId = params.projectId as string;
    const project = await this.projectRepo.findById(projectId);
    if (!project?.isActive()) {
      throw new BusinessException('Project not found or inactive');
    }
    return this.repo.create({
      projectId,
      title: params.title as string,
      category: params.category as RiskCategory,
      severity: params.severity as RiskSeverity,
      probability: params.probability as RiskProbability,
      identifiedDate: new Date(params.identifiedDate as Date),
      description: params.description as string | undefined,
      impact: params.impact as string | undefined,
      mitigationPlan: params.mitigationPlan as string | undefined,
      ownerId: params.ownerId as string | undefined,
    });
  }
}

@CommandHandler(UpdateProjectRiskCommand)
@Injectable()
export class UpdateProjectRiskHandler
  implements ICommandHandler<UpdateProjectRiskCommand, ProjectRiskRecord>
{
  constructor(@Inject(IProjectRiskRepository) private readonly repo: IProjectRiskRepository) {}

  async execute({ params }: UpdateProjectRiskCommand): Promise<ProjectRiskRecord> {
    const id = params.id as string;
    const risk = await this.repo.findById(id);
    if (!risk) {
      throw new BusinessException('Risk not found');
    }
    return this.repo.update(id, {
      title: params.title as string | undefined,
      severity: params.severity as RiskSeverity | undefined,
      probability: params.probability as RiskProbability | undefined,
      mitigationPlan: params.mitigationPlan as string | undefined,
      status: params.status as RiskStatus | undefined,
    });
  }
}

@CommandHandler(ResolveProjectRiskCommand)
@Injectable()
export class ResolveProjectRiskHandler
  implements ICommandHandler<ResolveProjectRiskCommand, ProjectRiskRecord>
{
  constructor(@Inject(IProjectRiskRepository) private readonly repo: IProjectRiskRepository) {}

  execute({ id }: ResolveProjectRiskCommand): Promise<ProjectRiskRecord> {
    return this.repo.resolve(id);
  }
}
