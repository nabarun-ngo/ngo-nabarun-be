import { IRepository } from '@nabarun-ngo/nestjs-shared-core';
import { Meeting, MeetingFilter } from '../aggregates/meeting/meeting.aggregate';

export const IMeetingRepository = Symbol('IMeetingRepository');

export interface IMeetingRepository extends IRepository<Meeting, string, MeetingFilter> {
  findByExtId(extId: string): Promise<Meeting | null>;
}
