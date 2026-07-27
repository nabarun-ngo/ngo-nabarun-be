import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { IWorkflowInboxRepository } from '../../../domain/ports/workflow-inbox.repository';
import { ReleaseUserInboxTasksCommand } from './release-user-inbox-tasks.command';

@CommandHandler(ReleaseUserInboxTasksCommand)
@Injectable()
export class ReleaseUserInboxTasksHandler
  implements ICommandHandler<ReleaseUserInboxTasksCommand, number>
{
  constructor(
    @Inject(IWorkflowInboxRepository)
    private readonly inboxRepo: IWorkflowInboxRepository,
  ) {}

  execute(command: ReleaseUserInboxTasksCommand): Promise<number> {
    return this.inboxRepo.releaseTasksForDeletedUser(command.userId);
  }
}
