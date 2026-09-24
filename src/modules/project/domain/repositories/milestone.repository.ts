import { MilestoneImportance, MilestoneStatus } from '../enums/milestone.enum';

export interface MilestoneRecord {
  id: string;
  projectId: string;
  name: string;
  description?: string | null;
  targetDate: Date;
  actualDate?: Date | null;
  status: MilestoneStatus;
  importance: MilestoneImportance;
  dependencies: string[];
  notes?: string | null;
  createdAt: Date;
  updatedAt: Date;
}

export interface MilestoneFilter {
  projectId?: string;
  status?: MilestoneStatus;
  excludeStatus?: MilestoneStatus;
}

export interface MilestoneSummary {
  id: string;
  name: string;
  targetDate: Date;
  status: string;
}

export const IMilestoneRepository = Symbol('IMilestoneRepository');

export interface IMilestoneRepository {
  count(filter: MilestoneFilter): Promise<number>;
  findByProjectId(projectId: string): Promise<MilestoneRecord[]>;
  findUpcomingSummariesByProjectId(projectId: string, limit: number): Promise<MilestoneSummary[]>;
  findById(id: string): Promise<MilestoneRecord | null>;
  create(data: {
    projectId: string;
    name: string;
    targetDate: Date;
    importance: MilestoneImportance;
    description?: string;
  }): Promise<MilestoneRecord>;
  update(
    id: string,
    data: {
      name?: string;
      targetDate?: Date;
      importance?: MilestoneImportance;
      description?: string;
    },
  ): Promise<MilestoneRecord>;
  complete(id: string): Promise<MilestoneRecord>;
}
