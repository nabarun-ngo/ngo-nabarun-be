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
import { canFulfill } from '../../utilities/request-eligibility';
import { StartRequestCommand } from './start-request.command';

@CommandHandler(StartRequestCommand)
@Injectable()
export class StartRequestHandler
  implements ICommandHandler<StartRequestCommand, RequestDto>
{
  constructor(
    @Inject(IRequestRepository)
    private readonly requests: IRequestRepository,
  ) {}

  async execute(command: StartRequestCommand): Promise<RequestDto> {
    const userId = requireUserId(command.user);
    const request = await this.requests.findById(command.id);
    if (!request) throw new RequestNotFoundError(command.id);
    if (request.status !== RequestStatus.YetToStart) {
      throw new RequestInvalidStateError('Only Yet to Start requests can be started');
    }
    if (!canFulfill(request, command.user)) {
      throw new RequestForbiddenError('You are not eligible to fulfill this request');
    }

    const updated = await this.requests.update(request.id, {
      status: RequestStatus.InProgress,
      claimedById: userId,
      claimedAt: new Date(),
      assigneeId: userId,
    });

    await this.requests.appendEvent({
      requestId: request.id,
      type: RequestEventType.Started,
      actorId: userId,
    });

    return toRequestDto(updated);
  }
}
