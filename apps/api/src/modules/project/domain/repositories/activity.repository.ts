import { IRepository } from '@nabarun-ngo/nestjs-shared-core';
import { Activity, ActivityFilter } from '../aggregates/activity/activity.aggregate';

export interface ActivitySummary {
  id: string;
  name: string;
  status: string;
  scale: string;
}

export const IActivityRepository = Symbol('IActivityRepository');

export interface IActivityRepository extends IRepository<Activity, string, ActivityFilter> {
  findRecentSummariesByProjectId(projectId: string, limit: number): Promise<ActivitySummary[]>;
}
