import { Injectable, Logger } from '@nestjs/common';
import { EventsHandler, IEventHandler } from '@nestjs/cqrs';
import { WorkflowFacade } from '@nabarun-ngo/nestjs-shared-workflow';
import { UserDeletedEvent } from '../../../../../modules/user/domain/events/user-deleted.event';

@Injectable()
@EventsHandler(UserDeletedEvent)
export class OnUserDeletedWorkflowHandler implements IEventHandler<UserDeletedEvent> {
  private readonly logger = new Logger(OnUserDeletedWorkflowHandler.name);

  constructor(private readonly workflowFacade: WorkflowFacade) {}

  async handle(event: UserDeletedEvent): Promise<void> {
    const count = await this.workflowFacade.releaseInboxTasksForDeletedUser(event.userId);

    if (count > 0) {
      this.logger.log(
        `Released ${count} workflow inbox task(s) after user delete: ${event.userId}`,
      );
    }
  }
}
