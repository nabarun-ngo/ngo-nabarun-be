import { Injectable } from '@nestjs/common';
import { randomUUID } from 'crypto';
import { BasePrismaService } from '@nabarun-ngo/nestjs-shared-persistence';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { PrismaClient } from '../../prisma/client';
import {
  ITeamMemberRepository,
  TeamMemberRecord,
} from '../../../../modules/project/domain/repositories/team-member.repository';
import { TeamMemberRole } from '../../../../modules/project/domain/enums/team-member.enum';

@Injectable()
export class TeamMemberPrismaRepository implements ITeamMemberRepository {
  constructor(private readonly db: BasePrismaService<PrismaClient>) { }

  async findByProjectId(projectId: string): Promise<TeamMemberRecord[]> {
    const rows = await this.db.client.projectTeamMember.findMany({
      where: { projectId, deletedAt: null },
      orderBy: { startDate: 'desc' },
    });
    return rows.map((row) => this.toRecord(row));
  }

  async findById(id: string): Promise<TeamMemberRecord | null> {
    const row = await this.db.client.projectTeamMember.findFirst({
      where: { id, deletedAt: null },
    });
    return row ? this.toRecord(row) : null;
  }

  async create(data: {
    projectId: string;
    userId: string;
    role: TeamMemberRole;
    startDate: Date;
    responsibilities?: string;
    hoursAllocated?: number;
  }): Promise<TeamMemberRecord> {
    const row = await this.db.client.projectTeamMember.create({
      data: {
        id: randomUUID(),
        projectId: data.projectId,
        userId: data.userId,
        role: data.role,
        startDate: data.startDate,
        responsibilities: data.responsibilities,
        hoursAllocated: data.hoursAllocated,
        isActive: true,
      },
    });
    return this.toRecord(row);
  }

  async update(
    id: string,
    data: { role?: TeamMemberRole; responsibilities?: string; hoursAllocated?: number },
  ): Promise<TeamMemberRecord> {
    const row = await this.db.client.projectTeamMember.update({ where: { id }, data });
    return this.toRecord(row);
  }

  async deactivate(id: string): Promise<TeamMemberRecord> {
    const member = await this.findById(id);
    if (!member) {
      throw new BusinessException('Team member not found');
    }
    const row = await this.db.client.projectTeamMember.update({
      where: { id },
      data: { isActive: false, endDate: new Date() },
    });
    return this.toRecord(row);
  }

  private toRecord(row: {
    id: string;
    projectId: string;
    userId: string;
    role: string;
    responsibilities: string | null;
    startDate: Date;
    endDate: Date | null;
    hoursAllocated: { toNumber(): number } | number | null;
    isActive: boolean;
    createdAt: Date;
    updatedAt: Date;
  }): TeamMemberRecord {
    return {
      id: row.id,
      projectId: row.projectId,
      userId: row.userId,
      role: row.role as TeamMemberRole,
      responsibilities: row.responsibilities,
      startDate: row.startDate,
      endDate: row.endDate,
      hoursAllocated:
        row.hoursAllocated == null
          ? null
          : typeof row.hoursAllocated === 'number'
            ? row.hoursAllocated
            : row.hoursAllocated.toNumber(),
      isActive: row.isActive,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    };
  }
}
