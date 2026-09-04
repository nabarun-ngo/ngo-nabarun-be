import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { IMeetingRepository } from '../../../domain/repositories/meeting.repository';
import { MeetingDetailDto } from '../../dtos/meeting.dto';
import { MeetingMapper } from '../../mappers/meeting.mapper';
import { ListMeetingsQuery } from './list-meetings.query';

@QueryHandler(ListMeetingsQuery)
@Injectable()
export class ListMeetingsHandler implements IQueryHandler<ListMeetingsQuery, PagedResponse<MeetingDetailDto>> {
  constructor(@Inject(IMeetingRepository) private readonly repo: IMeetingRepository) { }

  async execute(query: ListMeetingsQuery): Promise<PagedResponse<MeetingDetailDto>> {
    const { pageIndex, pageSize, sortBy, sortDir, ...props } = query.filter ?? {};
    const filter = new BaseFilter(props, pageIndex ?? 0, pageSize ?? 20, sortBy, sortDir);
    const page = await this.repo.findPaged({
      pageIndex: filter.pageIndex,
      pageSize: filter.pageSize,
      props: filter.props,
    });
    return {
      content: page.content.map(MeetingMapper.toDto),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
