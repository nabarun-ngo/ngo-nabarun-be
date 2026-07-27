import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, EventBus, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { ActivityStatus } from '../../../domain/enums/activity.enum';
import { Activity } from '../../../domain/aggregates/activity/activity.aggregate';
import { IActivityRepository } from '../../../domain/repositories/activity.repository';
import { FinanceFacade } from '../../../../finance/application/services/finance.facade';
import { UpdateActivityCommand } from './update-activity.command';

@CommandHandler(UpdateActivityCommand)
@Injectable()
export class UpdateActivityHandler implements ICommandHandler<UpdateActivityCommand, Activity> {
  constructor(
    @Inject(IActivityRepository) private readonly activityRepository: IActivityRepository,
    private readonly financeFacade: FinanceFacade,
    private readonly eventBus: EventBus,
  ) { }

  async execute({ params }: UpdateActivityCommand): Promise<Activity> {
    const activity = await this.activityRepository.findById(params.activityId);
    if (!activity) throw new BusinessException('Activity not found');
    activity.update(params);
    if (params.status) {
      if (params.status === ActivityStatus.COMPLETED) {
        await this.financeFacade.assertActivityCanClose(params.activityId);
      }
      activity.updateStatus(params.status);
    }
    const saved = await this.activityRepository.update(params.activityId, activity);
    const events = [...activity.domainEvents];
    activity.clearEvents();
    this.eventBus.publishAll(events);
    return saved;
  }
}
