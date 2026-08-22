import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { requireUserId } from '@nabarun-ngo/nestjs-shared-auth';
import { RequestEventType } from '../../../domain/enums/request-event-type.enum';
import { RequestStatus } from '../../../domain/enums/request-status.enum';
import {
  RequestForbiddenError,
  RequestInvalidStateError,
  RequestNotFoundError,
} from '../../../domain/errors/request.errors';
import { IRequestRepository } from '../../../domain/repositories/request.repository';
import { RequestDto } from '../../dtos/request.dto';
import { toRequestDto } from '../../mappers/request-response.mapper';
import { hasPermission } from '../../utilities/request-eligibility';
import { WithdrawRequestCommand } from './withdraw-request.command';

@CommandHandler(WithdrawRequestCommand)
@Injectable()
export class WithdrawRequestHandler
  implements ICommandHandler<WithdrawRequestCommand, RequestDto>
{
  constructor(
    @Inject(IRequestRepository)
    private readonly requests: IRequestRepository,
  ) {}

  async execute(command: WithdrawRequestCommand): Promise<RequestDto> {
    const userId = requireUserId(command.user);
    const request = await this.requests.findById(command.id);
    if (!request) throw new RequestNotFoundError(command.id);
    if (
      request.status !== RequestStatus.PendingForApproval &&
      request.status !== RequestStatus.YetToStart
    ) {
      throw new RequestInvalidStateError(
        'Only Pending for Approval or Yet to Start requests can be withdrawn',
      );
    }

    const isInitiator = request.initiatedById === userId;
    const canUpdate = hasPermission(command.user, 'update:requests');
    if (!isInitiator && !canUpdate) {
      throw new RequestForbiddenError('Only the initiator can withdraw this request');
    }

    const updated = await this.requests.update(request.id, {
      status: RequestStatus.Withdrawn,
      completedAt: new Date(),
      decisionNote: command.note ?? null,
    });

    await this.requests.appendEvent({
      requestId: request.id,
      type: RequestEventType.Withdrawn,
      actorId: userId,
      payload: { note: command.note ?? null },
    });

    return toRequestDto(updated);
  }
}
