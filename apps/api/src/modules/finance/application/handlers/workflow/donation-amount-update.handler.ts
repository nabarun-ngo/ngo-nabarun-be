import { Injectable, Logger } from '@nestjs/common';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { WorkflowFacade, WorkflowTaskHandler, WorkflowTaskHandlerContract } from '@nabarun-ngo/nestjs-shared-workflow';
import { UserFacade } from '../../../../user/application/services/user.facade';

@Injectable()
@WorkflowTaskHandler('DonationAmountUpdateHandler')
export class DonationAmountUpdateHandler implements WorkflowTaskHandlerContract {
  private readonly logger = new Logger(DonationAmountUpdateHandler.name);

  constructor(
    private readonly userFacade: UserFacade,
    private readonly workflowFacade: WorkflowFacade,
  ) {}

  async execute(params: {
    instanceId: string;
    elementId: string;
    input: Record<string, unknown>;
  }): Promise<void> {
    const newAmount = params.input.newAmount;
    if (newAmount == null) throw new BusinessException('Donation amount is required');

    const instance = await this.workflowFacade.getInstance(params.instanceId);
    const userId = instance.initiatedForId;
    if (!userId) throw new BusinessException('Workflow instance has no initiatedFor user');

    await this.userFacade.updateUserAdmin({
      userId,
      adminId: 'system',
      detail: {},
    });
    this.logger.log('Donation amount updated for user ' + userId);
  }
}
