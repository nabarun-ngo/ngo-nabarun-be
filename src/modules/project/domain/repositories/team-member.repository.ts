import { TeamMemberRole } from '../enums/team-member.enum';

export interface TeamMemberRecord {
  id: string;
  projectId: string;
  userId: string;
  role: TeamMemberRole;
  responsibilities?: string | null;
  startDate: Date;
  endDate?: Date | null;
  hoursAllocated?: number | null;
  isActive: boolean;
  createdAt: Date;
  updatedAt: Date;
}

export interface TeamMemberFilter {
  projectId?: string;
  isActive?: boolean;
}

export const ITeamMemberRepository = Symbol('ITeamMemberRepository');

export interface ITeamMemberRepository {
  findByProjectId(projectId: string): Promise<TeamMemberRecord[]>;
  findById(id: string): Promise<TeamMemberRecord | null>;
  create(data: {
    projectId: string;
    userId: string;
    role: TeamMemberRole;
    startDate: Date;
    responsibilities?: string;
    hoursAllocated?: number;
  }): Promise<TeamMemberRecord>;
  update(
    id: string,
    data: { role?: TeamMemberRole; responsibilities?: string; hoursAllocated?: number },
  ): Promise<TeamMemberRecord>;
  deactivate(id: string): Promise<TeamMemberRecord>;
}
