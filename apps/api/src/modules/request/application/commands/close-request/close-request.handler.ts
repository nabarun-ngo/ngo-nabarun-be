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
import { CloseRequestCommand } from './close-request.command';

@CommandHandler(CloseRequestCommand)
@Injectable()
export class CloseRequestHandler
  implements ICommandHandler<CloseRequestCommand, RequestDto>
{
  constructor(
    @Inject(IRequestRepository)
    private readonly requests: IRequestRepository,
  ) {}

  async execute(command: CloseRequestCommand): Promise<RequestDto> {
    const userId = requireUserId(command.user);
    const request = await this.requests.findById(command.id);
    if (!request) throw new RequestNotFoundError(command.id);
    if (request.status !== RequestStatus.InProgress) {
      throw new RequestInvalidStateError('Only In Progress requests can be closed');
    }
    if (request.claimedById !== userId && request.assigneeId !== userId) {
      throw new RequestForbiddenError('Only the assignee can close this request');
    }

    const updated = await this.requests.update(request.id, {
      status: RequestStatus.Closed,
      completedAt: new Date(),
      decisionNote: command.note ?? null,
    });

    await this.requests.appendEvent({
      requestId: request.id,
      type: RequestEventType.Closed,
      actorId: userId,
      payload: { note: command.note ?? null },
    });

    return toRequestDto(updated);
  }
}
