import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IRequestDefinitionPort } from '../../../domain/ports/request-definition.port';
import { RequestTypeDto } from '../../dtos/request.dto';
import { toRequestTypeDto } from '../../mappers/request-response.mapper';
import { ListRequestTypesQuery } from './list-request-types.query';

@QueryHandler(ListRequestTypesQuery)
@Injectable()
export class ListRequestTypesHandler
  implements IQueryHandler<ListRequestTypesQuery, RequestTypeDto[]>
{
  constructor(
    @Inject(IRequestDefinitionPort)
    private readonly definitions: IRequestDefinitionPort,
  ) {}

  async execute(): Promise<RequestTypeDto[]> {
    const defs = await this.definitions.listDefinitions();
    return defs.map(toRequestTypeDto);
  }
}
