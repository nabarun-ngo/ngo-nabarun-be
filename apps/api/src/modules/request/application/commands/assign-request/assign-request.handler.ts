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
import {
  canApprove,
  canFulfill,
  hasPermission,
} from '../../utilities/request-eligibility';
import { AssignRequestCommand } from './assign-request.command';

@CommandHandler(AssignRequestCommand)
@Injectable()
export class AssignRequestHandler
  implements ICommandHandler<AssignRequestCommand, RequestDto>
{
  constructor(
    @Inject(IRequestRepository)
    private readonly requests: IRequestRepository,
  ) {}

  async execute(command: AssignRequestCommand): Promise<RequestDto> {
    const userId = requireUserId(command.user);
    if (!hasPermission(command.user, 'update:requests')) {
      throw new RequestForbiddenError();
    }

    const request = await this.requests.findById(command.id);
    if (!request) throw new RequestNotFoundError(command.id);

    if (request.status === RequestStatus.PendingForApproval) {
      if (!canApprove(request, command.user)) {
        throw new RequestForbiddenError('You are not eligible to assign this request');
      }
      const updated = await this.requests.update(request.id, {
        assigneeId: command.assigneeId,
      });
      await this.requests.appendEvent({
        requestId: request.id,
        type: RequestEventType.Assigned,
        actorId: userId,
        payload: { assigneeId: command.assigneeId },
      });
      return toRequestDto(updated);
    }

    // Assigning before anyone starts is a routing hint: it names the intended
    // owner without taking the request out of the pool.
    if (request.status === RequestStatus.YetToStart) {
      if (!canFulfill(request, command.user)) {
        throw new RequestForbiddenError('You are not eligible to assign this request');
      }
      const updated = await this.requests.update(request.id, {
        assigneeId: command.assigneeId,
      });
      await this.requests.appendEvent({
        requestId: request.id,
        type: RequestEventType.Assigned,
        actorId: userId,
        payload: { assigneeId: command.assigneeId },
      });
      return toRequestDto(updated);
    }

    if (request.status === RequestStatus.InProgress) {
      const updated = await this.requests.update(request.id, {
        assigneeId: command.assigneeId,
        claimedById: command.assigneeId,
        claimedAt: new Date(),
      });
      await this.requests.appendEvent({
        requestId: request.id,
        type: RequestEventType.Assigned,
        actorId: userId,
        payload: { assigneeId: command.assigneeId },
      });
      return toRequestDto(updated);
    }

    throw new RequestInvalidStateError(
      'Only Pending for Approval, Yet to Start or In Progress requests can be assigned',
    );
  }
}
