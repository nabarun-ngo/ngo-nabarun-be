import { Injectable } from '@nestjs/common';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import {
  WorkflowFacade,
  WorkflowTaskHandler,
  WorkflowTaskHandlerContract,
} from '@nabarun-ngo/nestjs-shared-workflow';
import { UserFacade } from '../../services/user.facade';

@Injectable()
@WorkflowTaskHandler('UserDeleteAndDataCleanupHandler')
export class UserDeleteAndDataCleanupHandler implements WorkflowTaskHandlerContract {
  constructor(
    private readonly userFacade: UserFacade,
    private readonly workflowFacade: WorkflowFacade,
  ) {}

  async execute(params: {
    instanceId: string;
    elementId: string;
    input: Record<string, unknown>;
  }): Promise<Record<string, unknown>> {
    const instance = await this.workflowFacade.getInstance(params.instanceId);
    const userId =
      instance.initiatedForId ?? (await this.resolveUserIdFromContext(params.input));

    if (!userId) {
      throw new BusinessException(
        'Cannot delete user: workflow instance has no initiatedFor user and email lookup failed.',
      );
    }

    const adminId = instance.initiatedById ?? 'system';
    await this.userFacade.deleteUser({ userId, adminId });

    return { userDeleted: true, deletedUserId: userId };
  }

  private async resolveUserIdFromContext(
    input: Record<string, unknown>,
  ): Promise<string | null> {
    const email = typeof input.email === 'string' ? input.email.trim() : '';
    if (!email) {
      return null;
    }

    const user = await this.userFacade.findUserByEmail(email);
    return user?.id ?? null;
  }
}
