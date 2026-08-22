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
import { IRequestDefinitionPort } from '../../../domain/ports/request-definition.port';
import { IRequestRepository } from '../../../domain/repositories/request.repository';
import { RequestDto } from '../../dtos/request.dto';
import { toRequestDto } from '../../mappers/request-response.mapper';
import { canApprove } from '../../utilities/request-eligibility';
import { RejectRequestCommand } from './reject-request.command';

@CommandHandler(RejectRequestCommand)
@Injectable()
export class RejectRequestHandler
  implements ICommandHandler<RejectRequestCommand, RequestDto>
{
  constructor(
    @Inject(IRequestRepository)
    private readonly requests: IRequestRepository,
    @Inject(IRequestDefinitionPort)
    private readonly definitions: IRequestDefinitionPort,
  ) {}

  async execute(command: RejectRequestCommand): Promise<RequestDto> {
    const userId = requireUserId(command.user);
    const request = await this.requests.findById(command.id);
    if (!request) throw new RequestNotFoundError(command.id);
    if (request.status !== RequestStatus.PendingForApproval) {
      throw new RequestInvalidStateError('Only Pending for Approval requests can be rejected');
    }
    if (!request.needApproval) {
      throw new RequestInvalidStateError('This request does not require approval');
    }
    if (!canApprove(request, command.user)) {
      throw new RequestForbiddenError('You are not eligible to reject this request');
    }

    const updated = await this.requests.update(request.id, {
      status: RequestStatus.Rejected,
      completedAt: new Date(),
      decisionNote: command.note ?? null,
    });

    await this.requests.appendEvent({
      requestId: request.id,
      type: RequestEventType.Rejected,
      actorId: userId,
      payload: { note: command.note ?? null },
    });

    const definition = await this.definitions.getDefinition(request.type);
    return toRequestDto(updated, {
      executorInstructions: definition?.executorInstructions ?? null,
    });
  }
}
