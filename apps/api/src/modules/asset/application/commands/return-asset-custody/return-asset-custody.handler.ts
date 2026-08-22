import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { ReturnAssetCustodyCommand } from './return-asset-custody.command';
import { Asset } from '../../../domain/aggregates/asset/asset.aggregate';
import { IAssetRepository } from '../../../domain/repositories/asset.repository';

@CommandHandler(ReturnAssetCustodyCommand)
@Injectable()
export class ReturnAssetCustodyHandler implements ICommandHandler<ReturnAssetCustodyCommand, Asset> {
  constructor(@Inject(IAssetRepository) private readonly assetRepository: IAssetRepository) {}

  async execute({ params }: ReturnAssetCustodyCommand): Promise<Asset> {
    const asset = await this.assetRepository.findById(params.id);
    if (!asset) {
      throw new BusinessException('Asset not found with id ' + params.id);
    }
    asset.returnCustody(params.returnedById, params.notes);
    return this.assetRepository.update(asset.id, asset);
  }
}
