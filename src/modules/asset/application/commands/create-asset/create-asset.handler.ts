import { Inject, Injectable } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { CreateAssetCommand } from './create-asset.command';
import { Asset } from '../../../domain/aggregates/asset/asset.aggregate';
import { IAssetRepository } from '../../../domain/repositories/asset.repository';

@CommandHandler(CreateAssetCommand)
@Injectable()
export class CreateAssetHandler implements ICommandHandler<CreateAssetCommand, Asset> {
  constructor(@Inject(IAssetRepository) private readonly assetRepository: IAssetRepository) {}

  async execute({ params }: CreateAssetCommand): Promise<Asset> {
    const asset = Asset.create({
      name: params.name,
      category: params.category,
      serialNumber: params.serialNumber,
      location: params.location,
      status: params.status,
      projectId: params.projectId,
      expenseId: params.expenseId,
      purchaseDate: params.purchaseDate ? new Date(params.purchaseDate) : undefined,
      purchaseCost: params.purchaseCost,
      currency: params.currency,
      currentValue: params.currentValue,
      depreciationMethodNotes: params.depreciationMethodNotes,
      maintenanceNotes: params.maintenanceNotes,
      createdById: params.createdById,
    });
    return this.assetRepository.create(asset);
  }
}
