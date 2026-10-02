import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { RequestNotFoundError } from '../../../domain/errors/request.errors';
import { IRequestDefinitionPort } from '../../../domain/ports/request-definition.port';
import { IRequestRepository } from '../../../domain/repositories/request.repository';
import { RequestDto } from '../../dtos/request.dto';
import { toRequestDto } from '../../mappers/request-response.mapper';
import { GetRequestQuery } from './get-request.query';

@QueryHandler(GetRequestQuery)
@Injectable()
export class GetRequestHandler implements IQueryHandler<GetRequestQuery, RequestDto> {
  constructor(
    @Inject(IRequestRepository)
    private readonly requests: IRequestRepository,
    @Inject(IRequestDefinitionPort)
    private readonly definitions: IRequestDefinitionPort,
  ) {}

  async execute(query: GetRequestQuery): Promise<RequestDto> {
    const record = await this.requests.findById(query.id, true);
    if (!record) throw new RequestNotFoundError(query.id);

    const definition = await this.definitions.getDefinition(record.type);
    return toRequestDto(record, {
      executorInstructions: definition?.executorInstructions ?? null,
      actorUserId: query.user?.userId,
    });
  }
}
