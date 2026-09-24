import { RiskCategory, RiskProbability, RiskSeverity, RiskStatus } from '../enums/risk.enum';

export interface ProjectRiskRecord {
  id: string;
  projectId: string;
  title: string;
  description?: string | null;
  category: RiskCategory;
  severity: RiskSeverity;
  probability: RiskProbability;
  status: RiskStatus;
  impact?: string | null;
  mitigationPlan?: string | null;
  ownerId?: string | null;
  identifiedDate: Date;
  resolvedDate?: Date | null;
  notes?: string | null;
  createdAt: Date;
  updatedAt: Date;
}

export interface ProjectRiskFilter {
  projectId?: string;
  status?: RiskStatus;
  excludeStatus?: RiskStatus;
}

export const IProjectRiskRepository = Symbol('IProjectRiskRepository');

export interface IProjectRiskRepository {
  count(filter: ProjectRiskFilter): Promise<number>;
  findByProjectId(projectId: string): Promise<ProjectRiskRecord[]>;
  findById(id: string): Promise<ProjectRiskRecord | null>;
  create(data: {
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
  }): Promise<ProjectRiskRecord>;
  update(
    id: string,
    data: {
      title?: string;
      severity?: RiskSeverity;
      probability?: RiskProbability;
      mitigationPlan?: string;
      status?: RiskStatus;
    },
  ): Promise<ProjectRiskRecord>;
  resolve(id: string): Promise<ProjectRiskRecord>;
}
