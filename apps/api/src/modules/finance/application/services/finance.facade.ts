import { Injectable } from '@nestjs/common';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { LinkExpenseToActivityCommand } from '../commands/link-expense-to-activity/link-expense-to-activity.command';
import { AssertActivityCanCloseQuery } from '../queries/assert-activity-can-close/assert-activity-can-close.query';

@Injectable()
export class FinanceFacade {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) {}

  linkExpenseToActivity(params: {
    activityId: string;
    expenseId: string;
    activityName: string;
  }): Promise<void> {
    return this.commandBus.execute(new LinkExpenseToActivityCommand(params));
  }

  assertActivityCanClose(activityId: string): Promise<void> {
    return this.queryBus.execute(new AssertActivityCanCloseQuery(activityId));
  }
}
