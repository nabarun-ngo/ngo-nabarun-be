import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter } from '@nabarun-ngo/nestjs-shared-core';
import { ListAssetsQuery } from './list-assets.query';
import { IAssetRepository } from '../../../domain/repositories/asset.repository';
import { AssetMapper } from '../../mappers/asset.mapper';
import { AssetListResponseDto } from '../../dtos/asset.dto';

@QueryHandler(ListAssetsQuery)
@Injectable()
export class ListAssetsHandler implements IQueryHandler<ListAssetsQuery, AssetListResponseDto> {
  constructor(@Inject(IAssetRepository) private readonly repo: IAssetRepository) {}

  async execute(query: ListAssetsQuery): Promise<AssetListResponseDto> {
    const filter = new BaseFilter(query.filter, query.pageIndex ?? 0, query.pageSize ?? 20);
    const page = await this.repo.findPaged({
      pageIndex: filter.pageIndex,
      pageSize: filter.pageSize,
      props: filter.props,
    });
    return {
      items: page.content.map(AssetMapper.toDto),
      total: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
