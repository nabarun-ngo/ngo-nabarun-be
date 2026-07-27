import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler, QueryHandler, IQueryHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { TeamMemberRole } from '../../domain/enums/team-member.enum';
import { IProjectRepository } from '../../domain/repositories/project.repository';
import {
  ITeamMemberRepository,
  TeamMemberRecord,
} from '../../domain/repositories/team-member.repository';
import {
  AddTeamMemberCommand,
  DeactivateTeamMemberCommand,
  ListTeamMembersQuery,
  UpdateTeamMemberCommand,
} from '../project-subresource.commands';

@QueryHandler(ListTeamMembersQuery)
@Injectable()
export class ListTeamMembersHandler implements IQueryHandler<ListTeamMembersQuery> {
  constructor(@Inject(ITeamMemberRepository) private readonly repo: ITeamMemberRepository) {}

  async execute(query: ListTeamMembersQuery) {
    const items = await this.repo.findByProjectId(query.projectId);
    return { items, total: items.length };
  }
}

@CommandHandler(AddTeamMemberCommand)
@Injectable()
export class AddTeamMemberHandler implements ICommandHandler<AddTeamMemberCommand, TeamMemberRecord> {
  constructor(
    @Inject(ITeamMemberRepository) private readonly repo: ITeamMemberRepository,
    @Inject(IProjectRepository) private readonly projectRepo: IProjectRepository,
  ) {}

  async execute({ params }: AddTeamMemberCommand): Promise<TeamMemberRecord> {
    const project = await this.projectRepo.findById(params.projectId);
    if (!project?.isActive()) {
      throw new BusinessException('Project not found or inactive');
    }
    return this.repo.create({
      projectId: params.projectId,
      userId: params.userId,
      role: params.role as TeamMemberRole,
      startDate: new Date(params.startDate),
      responsibilities: params.responsibilities,
      hoursAllocated: params.hoursAllocated,
    });
  }
}

@CommandHandler(UpdateTeamMemberCommand)
@Injectable()
export class UpdateTeamMemberHandler
  implements ICommandHandler<UpdateTeamMemberCommand, TeamMemberRecord>
{
  constructor(@Inject(ITeamMemberRepository) private readonly repo: ITeamMemberRepository) {}

  async execute({ params }: UpdateTeamMemberCommand): Promise<TeamMemberRecord> {
    const member = await this.repo.findById(params.id);
    if (!member) {
      throw new BusinessException('Team member not found');
    }
    return this.repo.update(params.id, {
      role: params.role as TeamMemberRole | undefined,
      responsibilities: params.responsibilities,
      hoursAllocated: params.hoursAllocated,
    });
  }
}

@CommandHandler(DeactivateTeamMemberCommand)
@Injectable()
export class DeactivateTeamMemberHandler
  implements ICommandHandler<DeactivateTeamMemberCommand, TeamMemberRecord>
{
  constructor(@Inject(ITeamMemberRepository) private readonly repo: ITeamMemberRepository) {}

  execute({ id }: DeactivateTeamMemberCommand): Promise<TeamMemberRecord> {
    return this.repo.deactivate(id);
  }
}
