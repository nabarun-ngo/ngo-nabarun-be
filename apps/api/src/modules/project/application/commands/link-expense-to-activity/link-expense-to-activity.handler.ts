import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { IActivityRepository } from '../../../domain/repositories/activity.repository';
import { FinanceFacade } from '../../../../finance/application/services/finance.facade';
import { LinkExpenseToActivityCommand } from './link-expense-to-activity.command';

@CommandHandler(LinkExpenseToActivityCommand)
@Injectable()
export class LinkExpenseToActivityHandler implements ICommandHandler<LinkExpenseToActivityCommand, void> {
  constructor(
    @Inject(IActivityRepository) private readonly activityRepository: IActivityRepository,
    private readonly financeFacade: FinanceFacade,
  ) { }

  async execute({ params }: LinkExpenseToActivityCommand): Promise<void> {
    const activity = await this.activityRepository.findById(params.activityId);
    if (!activity) throw new BusinessException('Activity not found');
    await this.financeFacade.linkExpenseToActivity({
      activityId: params.activityId,
      expenseId: params.expenseId,
      activityName: activity.name,
    });
  }
}
