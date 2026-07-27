import { Inject, Injectable } from '@nestjs/common';
import { BusinessException, IUserLookupPort } from '@nabarun-ngo/nestjs-shared-core';
import {
  WorkflowTaskHandler,
  WorkflowTaskHandlerContract,
} from '@nabarun-ngo/nestjs-shared-workflow';

@Injectable()
@WorkflowTaskHandler('UserNotRegisteredTaskHandler')
export class UserNotRegisteredTaskHandler implements WorkflowTaskHandlerContract {
  constructor(@Inject(IUserLookupPort) private readonly userLookup: IUserLookupPort) {}

  async execute(params: {
    instanceId: string;
    elementId: string;
    input: Record<string, unknown>;
  }): Promise<void> {
    const email = params.input.email;
    if (typeof email !== 'string' || !email.trim()) {
      throw new BusinessException('Email is required for duplicate-user check.');
    }

    const existing = await this.userLookup.findByEmail(email.trim());
    if (existing) {
      throw new BusinessException(`User already registered: ${email}`);
    }
  }
}
