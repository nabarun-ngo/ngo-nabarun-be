import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { Injectable } from '@nestjs/common';
import { GetWorkflowTimelineQuery } from './get-workflow-timeline.query';
import { EventLogService } from '../../services/event-log.service';
import type { WorkflowEventLogEntry } from '../../../domain/ports/workflow-event-log.repository';

@QueryHandler(GetWorkflowTimelineQuery)
@Injectable()
export class GetWorkflowTimelineHandler
  implements IQueryHandler<GetWorkflowTimelineQuery, WorkflowEventLogEntry[]>
{
  constructor(private readonly eventLog: EventLogService) {}

  execute(query: GetWorkflowTimelineQuery): Promise<WorkflowEventLogEntry[]> {
    return this.eventLog.getTimeline(query.instanceId, query.options);
  }
}
