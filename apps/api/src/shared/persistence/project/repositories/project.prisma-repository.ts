import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { PrismaClient } from '../../prisma/client';
import type {
  ProjectWhereInput,
  ProjectWhereUniqueInput,
  ProjectUncheckedCreateInput,
  ProjectUncheckedUpdateInput,
  ProjectOrderByWithRelationInput,
} from '../../prisma/models/Project';
import { Project, ProjectFilter } from '../../../../modules/project/domain/aggregates/project/project.aggregate';
import { IProjectRepository } from '../../../../modules/project/domain/repositories/project.repository';
import { ProjectPersistence, ProjectPrismaMapper } from '../mapper/project-prisma.mapper';

const PROJECT_RELATIONS = { manager: true, sponsor: true } as const;

@Injectable()
export class ProjectPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'project',
    Project,
    string,
    ProjectFilter,
    ProjectPersistence,
    ProjectWhereInput,
    ProjectWhereUniqueInput,
    ProjectUncheckedCreateInput,
    ProjectUncheckedUpdateInput,
    ProjectOrderByWithRelationInput,
    typeof PROJECT_RELATIONS
  >
  implements IProjectRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'project');
  }

  protected toDomain(row: ProjectPersistence): Project {
    return ProjectPrismaMapper.toDomain(row)!;
  }

  protected toCreateInput(entity: Project): ProjectUncheckedCreateInput {
    return ProjectPrismaMapper.toCreate(entity);
  }

  protected toUpdateInput(_id: string, entity: Project): ProjectUncheckedUpdateInput {
    return ProjectPrismaMapper.toUpdate(entity);
  }

  protected toUniqueWhere(id: string): ProjectWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(props?: ProjectFilter): ProjectWhereInput {
    return {
      ...(props?.status ? { status: props.status } : {}),
      ...(props?.category ? { category: props.category } : {}),
      ...(props?.phase ? { phase: props.phase } : {}),
      ...(props?.managerId ? { managerId: props.managerId } : {}),
      ...(props?.sponsorId ? { sponsorId: props.sponsorId } : {}),
      ...(props?.location ? { location: { contains: props.location, mode: 'insensitive' } } : {}),
      ...(props?.tags?.length ? { tags: { hasSome: props.tags } } : {}),
      ...(props?.isPublic !== undefined ? { isPublic: props.isPublic } : {}),
      deletedAt: null,
    };
  }

  protected override defaultOrderBy(): ProjectOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected override toInclude(): typeof PROJECT_RELATIONS {
    return PROJECT_RELATIONS;
  }

  protected override defaultPageSize(): number {
    return 20;
  }

  async findByCode(code: string): Promise<Project | null> {
    const row = await this.delegate.findUnique({
      where: { code },
      include: PROJECT_RELATIONS,
    });
    return ProjectPrismaMapper.toDomain(row);
  }

  override async create(entity: Project): Promise<Project> {
    const row = await this.delegate.create({
      data: ProjectPrismaMapper.toCreate(entity),
      include: PROJECT_RELATIONS,
    });
    return ProjectPrismaMapper.toDomain(row)!;
  }

  override async update(id: string, entity: Project): Promise<Project> {
    const row = await this.delegate.update({
      where: { id },
      data: ProjectPrismaMapper.toUpdate(entity),
      include: PROJECT_RELATIONS,
    });
    return ProjectPrismaMapper.toDomain(row)!;
  }

  override async delete(id: string): Promise<void> {
    await this.delegate.update({ where: { id }, data: { deletedAt: new Date() } });
  }
}
