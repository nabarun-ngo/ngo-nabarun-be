import { Injectable } from '@nestjs/common';
import { EventsHandler, IEventHandler } from '@nestjs/cqrs';
import { QueueFacade } from '@nabarun-ngo/nestjs-shared-queue';
import { ActivityCompletedEvent } from '../../../../domain/events/activity-completed.event';
import { TriggerReportGenerationJob } from '../../../../../reporting/application/handlers/queue/trigger-report-generation.job';

@Injectable()
@EventsHandler(ActivityCompletedEvent)
export class OnActivityCompletedHandler implements IEventHandler<ActivityCompletedEvent> {
  constructor(private readonly queueFacade: QueueFacade) {}

  async handle(event: ActivityCompletedEvent): Promise<void> {
    await this.queueFacade.dispatch(
      new TriggerReportGenerationJob('ACTIVITY_REPORT', { activityId: event.snapshot.activityId }),
    );
  }
}
