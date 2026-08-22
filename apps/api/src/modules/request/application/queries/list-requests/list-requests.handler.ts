import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { requireUserId } from '@nabarun-ngo/nestjs-shared-auth';
import { IRequestRepository } from '../../../domain/repositories/request.repository';
import { RequestDto } from '../../dtos/request.dto';
import { toRequestDto } from '../../mappers/request-response.mapper';
import { actorGroups, actorPermissions, actorRoles } from '../../utilities/request-eligibility';
import { ListRequestsQuery } from './list-requests.query';

@QueryHandler(ListRequestsQuery)
@Injectable()
export class ListRequestsHandler
  implements IQueryHandler<ListRequestsQuery, PagedResponse<RequestDto>>
{
  constructor(
    @Inject(IRequestRepository)
    private readonly requests: IRequestRepository,
  ) {}

  async execute(query: ListRequestsQuery): Promise<PagedResponse<RequestDto>> {
    const userId = requireUserId(query.user);
    const pageIndex = query.query.pageIndex ?? 0;
    const pageSize = query.query.pageSize ?? 20;
    const page = await this.requests.findPaged(
      {
        scope: query.query.scope,
        type: query.query.type,
        status: query.query.status,
        id: query.query.id,
        actorUserId: userId,
        actorRoles: actorRoles(query.user),
        actorGroups: actorGroups(query.user),
        actorPermissions: actorPermissions(query.user),
      },
      pageIndex,
      pageSize,
    );
    return new PagedResponse(
      page.content.map((r) => toRequestDto(r, { actorUserId: userId })),
      page.totalSize,
      page.pageIndex,
      page.pageSize,
    );
  }
}
