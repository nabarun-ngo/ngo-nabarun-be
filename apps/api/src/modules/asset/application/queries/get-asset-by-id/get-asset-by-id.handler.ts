import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { GetAssetByIdQuery } from './get-asset-by-id.query';
import { IAssetRepository } from '../../../domain/repositories/asset.repository';
import { AssetMapper } from '../../mappers/asset.mapper';
import { AssetDetailDto } from '../../dtos/asset.dto';

@QueryHandler(GetAssetByIdQuery)
@Injectable()
export class GetAssetByIdHandler implements IQueryHandler<GetAssetByIdQuery, AssetDetailDto> {
  constructor(@Inject(IAssetRepository) private readonly repo: IAssetRepository) {}

  async execute(query: GetAssetByIdQuery): Promise<AssetDetailDto> {
    const asset = await this.repo.findById(query.id);
    if (!asset) {
      throw new BusinessException('Asset not found with id ' + query.id);
    }
    return AssetMapper.toDto(asset);
  }
}
