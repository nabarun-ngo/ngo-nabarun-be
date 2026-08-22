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
import { ApproveRequestCommand } from './approve-request.command';

@CommandHandler(ApproveRequestCommand)
@Injectable()
export class ApproveRequestHandler
  implements ICommandHandler<ApproveRequestCommand, RequestDto>
{
  constructor(
    @Inject(IRequestRepository)
    private readonly requests: IRequestRepository,
    @Inject(IRequestDefinitionPort)
    private readonly definitions: IRequestDefinitionPort,
  ) {}

  async execute(command: ApproveRequestCommand): Promise<RequestDto> {
    const userId = requireUserId(command.user);
    const request = await this.requests.findById(command.id);
    if (!request) throw new RequestNotFoundError(command.id);
    if (request.status !== RequestStatus.PendingForApproval) {
      throw new RequestInvalidStateError('Only Pending for Approval requests can be approved');
    }
    if (!request.needApproval) {
      throw new RequestInvalidStateError('This request does not require approval');
    }
    if (!canApprove(request, command.user)) {
      throw new RequestForbiddenError('You are not eligible to approve this request');
    }

    const updated = await this.requests.update(request.id, {
      status: RequestStatus.YetToStart,
      assigneeId: null,
      claimedById: null,
      claimedAt: null,
      decisionNote: command.note ?? null,
    });

    await this.requests.appendEvent({
      requestId: request.id,
      type: RequestEventType.Approved,
      actorId: userId,
      payload: { note: command.note ?? null },
    });

    const definition = await this.definitions.getDefinition(request.type);
    return toRequestDto(updated, {
      executorInstructions: definition?.executorInstructions ?? null,
    });
  }
}
