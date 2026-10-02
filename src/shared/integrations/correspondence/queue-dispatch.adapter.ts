import { Injectable } from '@nestjs/common';
import { QueueFacade } from '@ssdev-toolkit/nestjs-queue';
import {
  IDispatchQueuePort,
  CorrespondenceDispatchPayload,
} from '@ssdev-toolkit/nestjs-correspondence';
import { CorrespondenceDispatchJob } from '@ssdev-toolkit/nestjs-correspondence/application/jobs/correspondence-dispatch.job';

@Injectable()
export class QueueDispatchAdapter implements IDispatchQueuePort {
  constructor(private readonly queueFacade: QueueFacade) { }

  async enqueue(payload: CorrespondenceDispatchPayload): Promise<void> {
    await this.queueFacade.dispatch(new CorrespondenceDispatchJob(payload), {
      jobId: payload.dispatchId,
    });
  }
}

export const DISPATCH_QUEUE_PORT_PROVIDER = {
  provide: IDispatchQueuePort,
  useClass: QueueDispatchAdapter,
};
