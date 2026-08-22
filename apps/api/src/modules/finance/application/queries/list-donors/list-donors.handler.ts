import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter, IUserLookupPort } from '@nabarun-ngo/nestjs-shared-core';
import { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { DonorType } from '../../../domain/enums/donor-type.enum';
import { DonorListResponseDto } from '../../dtos/donor.dto';
import { DonorMapper } from '../../mappers/donor.mapper';
import { ListDonorsQuery } from './list-donors.query';

@QueryHandler(ListDonorsQuery)
@Injectable()
export class ListDonorsHandler implements IQueryHandler<ListDonorsQuery, DonorListResponseDto> {
  constructor(
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    @Inject(IUserLookupPort) private readonly userLookup: IUserLookupPort,
  ) {}

  async execute(query: ListDonorsQuery): Promise<DonorListResponseDto> {
    const filter = new BaseFilter(
      {
        q: query.filter.q,
        type: query.filter.type,
        status: query.filter.status ? [query.filter.status] : undefined,
      },
      query.pageIndex ?? 0,
      query.pageSize ?? 20,
      query.sortBy,
      query.sortDir,
    );
    const page = await this.donorRepository.findPaged(filter);
    const memberIds = page.content
      .filter((d) => d.type === DonorType.MEMBER && d.userProfileId)
      .map((d) => d.userProfileId!);
    const users = memberIds.length > 0 ? await this.userLookup.findByIds(memberIds) : [];
    const userMap = new Map(users.map((u) => [u.id, u]));

    return {
      items: page.content.map((d) =>
        DonorMapper.toDto(d, d.userProfileId ? userMap.get(d.userProfileId) : undefined),
      ),
      total: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
