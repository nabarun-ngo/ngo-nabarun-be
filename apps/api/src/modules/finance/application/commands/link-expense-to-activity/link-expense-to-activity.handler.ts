import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { Inject, Injectable } from '@nestjs/common';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { ExpenseRefType } from '../../../domain/enums/expense.enum';
import { IExpenseRepository } from '../../../domain/repositories/expense.repository';
import { LinkExpenseToActivityCommand } from './link-expense-to-activity.command';

@CommandHandler(LinkExpenseToActivityCommand)
@Injectable()
export class LinkExpenseToActivityHandler
  implements ICommandHandler<LinkExpenseToActivityCommand, void>
{
  constructor(@Inject(IExpenseRepository) private readonly expenseRepository: IExpenseRepository) {}

  async execute(command: LinkExpenseToActivityCommand): Promise<void> {
    const expense = await this.expenseRepository.findById(command.params.expenseId);
    if (!expense) throw new BusinessException('Expense not found');
    expense.linkToReference(
      command.params.activityId,
      ExpenseRefType.EVENT,
      command.params.activityName,
    );
    await this.expenseRepository.update(command.params.expenseId, expense);
  }
}
