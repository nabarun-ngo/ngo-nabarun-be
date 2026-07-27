import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler, QueryHandler, IQueryHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { MilestoneImportance } from '../../domain/enums/milestone.enum';
import { IMilestoneRepository, MilestoneRecord } from '../../domain/repositories/milestone.repository';
import { IProjectRepository } from '../../domain/repositories/project.repository';
import {
  CompleteMilestoneCommand,
  CreateMilestoneCommand,
  ListMilestonesQuery,
  UpdateMilestoneCommand,
} from '../project-subresource.commands';

@QueryHandler(ListMilestonesQuery)
@Injectable()
export class ListMilestonesHandler implements IQueryHandler<ListMilestonesQuery> {
  constructor(@Inject(IMilestoneRepository) private readonly repo: IMilestoneRepository) {}

  async execute(query: ListMilestonesQuery) {
    const items = await this.repo.findByProjectId(query.projectId);
    return { items, total: items.length };
  }
}

@CommandHandler(CreateMilestoneCommand)
@Injectable()
export class CreateMilestoneHandler
  implements ICommandHandler<CreateMilestoneCommand, MilestoneRecord>
{
  constructor(
    @Inject(IMilestoneRepository) private readonly repo: IMilestoneRepository,
    @Inject(IProjectRepository) private readonly projectRepo: IProjectRepository,
  ) {}

  async execute({ params }: CreateMilestoneCommand): Promise<MilestoneRecord> {
    const projectId = params.projectId as string;
    const project = await this.projectRepo.findById(projectId);
    if (!project?.isActive()) {
      throw new BusinessException('Project not found or inactive');
    }
    return this.repo.create({
      projectId,
      name: params.name as string,
      targetDate: new Date(params.targetDate as Date),
      importance: params.importance as MilestoneImportance,
      description: params.description as string | undefined,
    });
  }
}

@CommandHandler(UpdateMilestoneCommand)
@Injectable()
export class UpdateMilestoneHandler
  implements ICommandHandler<UpdateMilestoneCommand, MilestoneRecord>
{
  constructor(@Inject(IMilestoneRepository) private readonly repo: IMilestoneRepository) {}

  async execute({ params }: UpdateMilestoneCommand): Promise<MilestoneRecord> {
    const id = params.id as string;
    const milestone = await this.repo.findById(id);
    if (!milestone) {
      throw new BusinessException('Milestone not found');
    }
    return this.repo.update(id, {
      name: params.name as string | undefined,
      targetDate: params.targetDate ? new Date(params.targetDate as Date) : undefined,
      importance: params.importance as MilestoneImportance | undefined,
      description: params.description as string | undefined,
    });
  }
}

@CommandHandler(CompleteMilestoneCommand)
@Injectable()
export class CompleteMilestoneHandler
  implements ICommandHandler<CompleteMilestoneCommand, MilestoneRecord>
{
  constructor(@Inject(IMilestoneRepository) private readonly repo: IMilestoneRepository) {}

  execute({ id }: CompleteMilestoneCommand): Promise<MilestoneRecord> {
    return this.repo.complete(id);
  }
}
