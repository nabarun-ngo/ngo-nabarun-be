import { Injectable } from '@nestjs/common';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { WorkflowFacade, WorkflowTaskHandler, WorkflowTaskHandlerContract } from '@nabarun-ngo/nestjs-shared-workflow';
import { UserFacade } from '../../../../user/application/services/user.facade';

@Injectable()
@WorkflowTaskHandler('DonationPauseUpdateHandler')
export class DonationPauseUpdateHandler implements WorkflowTaskHandlerContract {
  constructor(
    private readonly userFacade: UserFacade,
    private readonly workflowFacade: WorkflowFacade,
  ) {}

  async execute(params: {
    instanceId: string;
    elementId: string;
    input: Record<string, unknown>;
  }): Promise<void> {
    if (!params.input.startDate || !params.input.endDate) {
      throw new BusinessException('Start date and end date are required');
    }
    const instance = await this.workflowFacade.getInstance(params.instanceId);
    const userId = instance.initiatedForId;
    if (!userId) throw new BusinessException('Workflow instance has no initiatedFor user');

    await this.userFacade.updateUserAdmin({
      userId,
      adminId: 'system',
      detail: {},
    });
  }
}
