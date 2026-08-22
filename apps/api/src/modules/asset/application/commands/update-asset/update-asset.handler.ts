import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { UpdateAssetCommand } from './update-asset.command';
import { Asset } from '../../../domain/aggregates/asset/asset.aggregate';
import { IAssetRepository } from '../../../domain/repositories/asset.repository';

@CommandHandler(UpdateAssetCommand)
@Injectable()
export class UpdateAssetHandler implements ICommandHandler<UpdateAssetCommand, Asset> {
  constructor(@Inject(IAssetRepository) private readonly assetRepository: IAssetRepository) {}

  async execute({ params }: UpdateAssetCommand): Promise<Asset> {
    const asset = await this.assetRepository.findById(params.id);
    if (!asset) {
      throw new BusinessException('Asset not found with id ' + params.id);
    }

    asset.update({
      name: params.name,
      category: params.category,
      serialNumber: params.serialNumber,
      location: params.location,
      status: params.status,
      projectId: params.projectId,
      expenseId: params.expenseId,
      purchaseDate: params.purchaseDate !== undefined ? new Date(params.purchaseDate) : undefined,
      purchaseCost: params.purchaseCost,
      currency: params.currency,
      currentValue: params.currentValue,
      depreciationMethodNotes: params.depreciationMethodNotes,
      maintenanceNotes: params.maintenanceNotes,
      updatedById: params.updatedById,
    });

    return this.assetRepository.update(asset.id, asset);
  }
}
