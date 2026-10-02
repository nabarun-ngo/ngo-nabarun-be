import { Asset } from '../../domain/aggregates/asset/asset.aggregate';
import { AssetDetailDto } from '../dtos/asset.dto';

export class AssetMapper {
  static toDto(asset: Asset): AssetDetailDto {
    return {
      id: asset.id,
      name: asset.name,
      category: asset.category,
      serialNumber: asset.serialNumber,
      location: asset.location,
      status: asset.status,
      custodianUserId: asset.custodianUserId,
      projectId: asset.projectId,
      expenseId: asset.expenseId,
      purchaseDate: asset.purchaseDate,
      purchaseCost: asset.purchaseCost,
      currency: asset.currency,
      currentValue: asset.currentValue,
      depreciationMethodNotes: asset.depreciationMethodNotes,
      maintenanceNotes: asset.maintenanceNotes,
      createdById: asset.createdById,
      updatedById: asset.updatedById,
      custodyHistory: asset.custodyHistory,
      createdAt: asset.createdAt,
      updatedAt: asset.updatedAt,
    };
  }
}
