import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { AssignAssetCustodyCommand } from './assign-asset-custody.command';
import { Asset } from '../../../domain/aggregates/asset/asset.aggregate';
import { IAssetRepository } from '../../../domain/repositories/asset.repository';

@CommandHandler(AssignAssetCustodyCommand)
@Injectable()
export class AssignAssetCustodyHandler implements ICommandHandler<AssignAssetCustodyCommand, Asset> {
  constructor(@Inject(IAssetRepository) private readonly assetRepository: IAssetRepository) {}

  async execute({ params }: AssignAssetCustodyCommand): Promise<Asset> {
    const asset = await this.assetRepository.findById(params.id);
    if (!asset) {
      throw new BusinessException('Requested asset not found.');
    }
    asset.assignCustody(params.custodianUserId, params.assignedById, params.notes);
    return this.assetRepository.update(asset.id, asset);
  }
}
