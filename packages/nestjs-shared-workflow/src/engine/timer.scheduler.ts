import { Inject, Injectable } from '@nestjs/common';
import { DateTime } from 'luxon';
import { WORKFLOW_OPTIONS } from '../infrastructure/workflow-options.token';
import type { WorkflowModuleOptions } from '../workflow.schema';
import { WORKFLOW_QUEUE_PORT, IWorkflowQueuePort } from '../domain/ports/workflow-queue.port';

export interface ScheduleSlaParams {
  instanceId: string;
  elementId: string;
  slaHours: number;
  correlationId?: string;
}

@Injectable()
export class TimerScheduler {
  constructor(
    @Inject(WORKFLOW_QUEUE_PORT)
    private readonly queuePort: IWorkflowQueuePort,
    @Inject(WORKFLOW_OPTIONS)
    private readonly options: WorkflowModuleOptions,
  ) {}

  computeSlaDeadline(slaHours: number): Date {
    return DateTime.now()
      .setZone(this.options.defaultTimezone ?? 'UTC')
      .plus({ hours: slaHours })
      .toJSDate();
  }

  async scheduleSlaDeadline(
    params: ScheduleSlaParams,
  ): Promise<{ id: string; runAt: Date }> {
    const runAt = this.computeSlaDeadline(params.slaHours);

    const port = this.queuePort as IWorkflowQueuePort & {
      enqueueTimer?: (
        payload: {
          instanceId: string;
          elementId: string;
          correlationId?: string;
        },
        options?: { dedupeKey?: string; runAt?: Date },
      ) => Promise<{ id: string }>;
    };

    if (typeof port.enqueueTimer === 'function') {
      const result = await port.enqueueTimer(
        {
          instanceId: params.instanceId,
          elementId: params.elementId,
          correlationId: params.correlationId,
        },
        {
          dedupeKey: `sla:${params.instanceId}:${params.elementId}`,
          runAt,
        },
      );
      return { ...result, runAt };
    }

    const result = await this.queuePort.enqueue(
      'slaEscalation',
      {
        instanceId: params.instanceId,
        elementId: params.elementId,
        correlationId: params.correlationId,
      },
      {
        dedupeKey: `sla:${params.instanceId}:${params.elementId}`,
        runAt,
      },
    );
    return { ...result, runAt };
  }
}
