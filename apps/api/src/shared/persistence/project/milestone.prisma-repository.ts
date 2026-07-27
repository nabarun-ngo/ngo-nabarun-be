import { Injectable } from '@nestjs/common';
import { randomUUID } from 'crypto';
import { BasePrismaService } from '@nabarun-ngo/nestjs-shared-persistence';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { PrismaClient } from '../prisma/client';
import {
  IMilestoneRepository,
  MilestoneFilter,
  MilestoneRecord,
} from '../../../modules/project/domain/repositories/milestone.repository';
import {
  MilestoneImportance,
  MilestoneStatus,
} from '../../../modules/project/domain/enums/milestone.enum';

@Injectable()
export class MilestonePrismaRepository implements IMilestoneRepository {
  constructor(private readonly db: BasePrismaService<PrismaClient>) {}

  async count(filter: MilestoneFilter): Promise<number> {
    return this.db.client.milestone.count({ where: this.where(filter) });
  }

  async findUpcomingSummariesByProjectId(projectId: string, limit: number) {
    return this.db.client.milestone.findMany({
      where: {
        projectId,
        deletedAt: null,
        status: { not: MilestoneStatus.ACHIEVED },
      },
      orderBy: { targetDate: 'asc' },
      take: limit,
      select: { id: true, name: true, targetDate: true, status: true },
    });
  }

  async findByProjectId(projectId: string): Promise<MilestoneRecord[]> {
    const rows = await this.db.client.milestone.findMany({
      where: { projectId, deletedAt: null },
      orderBy: { targetDate: 'asc' },
    });
    return rows.map((row) => this.toRecord(row));
  }

  async findById(id: string): Promise<MilestoneRecord | null> {
    const row = await this.db.client.milestone.findFirst({
      where: { id, deletedAt: null },
    });
    return row ? this.toRecord(row) : null;
  }

  async create(data: {
    projectId: string;
    name: string;
    targetDate: Date;
    importance: MilestoneImportance;
    description?: string;
  }): Promise<MilestoneRecord> {
    const row = await this.db.client.milestone.create({
      data: {
        id: randomUUID(),
        projectId: data.projectId,
        name: data.name,
        targetDate: data.targetDate,
        importance: data.importance,
        description: data.description,
        status: MilestoneStatus.PENDING,
        dependencies: [],
      },
    });
    return this.toRecord(row);
  }

  async update(
    id: string,
    data: {
      name?: string;
      targetDate?: Date;
      importance?: MilestoneImportance;
      description?: string;
    },
  ): Promise<MilestoneRecord> {
    const row = await this.db.client.milestone.update({ where: { id }, data });
    return this.toRecord(row);
  }

  async complete(id: string): Promise<MilestoneRecord> {
    const milestone = await this.findById(id);
    if (!milestone) {
      throw new BusinessException('Milestone not found');
    }
    const row = await this.db.client.milestone.update({
      where: { id },
      data: { status: MilestoneStatus.ACHIEVED, actualDate: new Date() },
    });
    return this.toRecord(row);
  }

  private where(filter: MilestoneFilter) {
    return {
      deletedAt: null,
      ...(filter.projectId ? { projectId: filter.projectId } : {}),
      ...(filter.status ? { status: filter.status } : {}),
      ...(filter.excludeStatus ? { status: { not: filter.excludeStatus } } : {}),
    };
  }

  private toRecord(row: {
    id: string;
    projectId: string;
    name: string;
    description: string | null;
    targetDate: Date;
    actualDate: Date | null;
    status: string;
    importance: string;
    dependencies: string[];
    notes: string | null;
    createdAt: Date;
    updatedAt: Date;
  }): MilestoneRecord {
    return {
      id: row.id,
      projectId: row.projectId,
      name: row.name,
      description: row.description,
      targetDate: row.targetDate,
      actualDate: row.actualDate,
      status: row.status as MilestoneStatus,
      importance: row.importance as MilestoneImportance,
      dependencies: row.dependencies,
      notes: row.notes,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    };
  }
}
