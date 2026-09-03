import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter } from '@nabarun-ngo/nestjs-shared-core';
import { ListAssetsQuery } from './list-assets.query';
import { IAssetRepository } from '../../../domain/repositories/asset.repository';
import { AssetMapper } from '../../mappers/asset.mapper';
import { PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { AssetDetailDto } from '../../dtos/asset.dto';

@QueryHandler(ListAssetsQuery)
@Injectable()
export class ListAssetsHandler implements IQueryHandler<ListAssetsQuery, PagedResponse<AssetDetailDto>> {
  constructor(@Inject(IAssetRepository) private readonly repo: IAssetRepository) { }

  async execute(query: ListAssetsQuery): Promise<PagedResponse<AssetDetailDto>> {
    const filter = new BaseFilter(query.filter, query.filter?.pageIndex ?? 0, query.filter?.pageSize ?? 20);
    const page = await this.repo.findPaged({
      pageIndex: filter.pageIndex,
      pageSize: filter.pageSize,
      props: filter.props,
    });
    return {
      content: page.content.map(AssetMapper.toDto),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
