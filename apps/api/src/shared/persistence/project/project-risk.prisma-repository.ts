import { Injectable } from '@nestjs/common';
import { randomUUID } from 'crypto';
import { BasePrismaService } from '@nabarun-ngo/nestjs-shared-persistence';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { PrismaClient } from '../prisma/client';
import {
  IProjectRiskRepository,
  ProjectRiskFilter,
  ProjectRiskRecord,
} from '../../../modules/project/domain/repositories/project-risk.repository';
import {
  RiskCategory,
  RiskProbability,
  RiskSeverity,
  RiskStatus,
} from '../../../modules/project/domain/enums/risk.enum';

@Injectable()
export class ProjectRiskPrismaRepository implements IProjectRiskRepository {
  constructor(private readonly db: BasePrismaService<PrismaClient>) {}

  async count(filter: ProjectRiskFilter): Promise<number> {
    return this.db.client.projectRisk.count({ where: this.where(filter) });
  }

  async findByProjectId(projectId: string): Promise<ProjectRiskRecord[]> {
    const rows = await this.db.client.projectRisk.findMany({
      where: { projectId, deletedAt: null },
      orderBy: { identifiedDate: 'desc' },
    });
    return rows.map((row) => this.toRecord(row));
  }

  async findById(id: string): Promise<ProjectRiskRecord | null> {
    const row = await this.db.client.projectRisk.findFirst({
      where: { id, deletedAt: null },
    });
    return row ? this.toRecord(row) : null;
  }

  async create(data: {
    projectId: string;
    title: string;
    category: RiskCategory;
    severity: RiskSeverity;
    probability: RiskProbability;
    identifiedDate: Date;
    description?: string;
    impact?: string;
    mitigationPlan?: string;
    ownerId?: string;
  }): Promise<ProjectRiskRecord> {
    const row = await this.db.client.projectRisk.create({
      data: {
        id: randomUUID(),
        projectId: data.projectId,
        title: data.title,
        category: data.category,
        severity: data.severity,
        probability: data.probability,
        identifiedDate: data.identifiedDate,
        description: data.description,
        impact: data.impact,
        mitigationPlan: data.mitigationPlan,
        ownerId: data.ownerId,
        status: RiskStatus.IDENTIFIED,
      },
    });
    return this.toRecord(row);
  }

  async update(
    id: string,
    data: {
      title?: string;
      severity?: RiskSeverity;
      probability?: RiskProbability;
      mitigationPlan?: string;
      status?: RiskStatus;
    },
  ): Promise<ProjectRiskRecord> {
    const row = await this.db.client.projectRisk.update({ where: { id }, data });
    return this.toRecord(row);
  }

  async resolve(id: string): Promise<ProjectRiskRecord> {
    const risk = await this.findById(id);
    if (!risk) {
      throw new BusinessException('Risk not found');
    }
    if (
      (risk.severity === RiskSeverity.HIGH || risk.severity === RiskSeverity.CRITICAL) &&
      !risk.mitigationPlan
    ) {
      throw new BusinessException('Mitigation plan required for high/critical risks');
    }
    const row = await this.db.client.projectRisk.update({
      where: { id },
      data: { status: RiskStatus.CLOSED, resolvedDate: new Date() },
    });
    return this.toRecord(row);
  }

  private where(filter: ProjectRiskFilter) {
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
    title: string;
    description: string | null;
    category: string;
    severity: string;
    probability: string;
    status: string;
    impact: string | null;
    mitigationPlan: string | null;
    ownerId: string | null;
    identifiedDate: Date;
    resolvedDate: Date | null;
    notes: string | null;
    createdAt: Date;
    updatedAt: Date;
  }): ProjectRiskRecord {
    return {
      id: row.id,
      projectId: row.projectId,
      title: row.title,
      description: row.description,
      category: row.category as RiskCategory,
      severity: row.severity as RiskSeverity,
      probability: row.probability as RiskProbability,
      status: row.status as RiskStatus,
      impact: row.impact,
      mitigationPlan: row.mitigationPlan,
      ownerId: row.ownerId,
      identifiedDate: row.identifiedDate,
      resolvedDate: row.resolvedDate,
      notes: row.notes,
      createdAt: row.createdAt,
      updatedAt: row.updatedAt,
    };
  }
}
