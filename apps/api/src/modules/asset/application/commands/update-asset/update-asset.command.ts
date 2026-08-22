import { AssetCategory, AssetStatus } from '../../../domain/enums/asset.enum';

export class UpdateAssetCommand {
  constructor(
    public readonly params: {
      id: string;
      name?: string;
      category?: AssetCategory;
      serialNumber?: string;
      location?: string;
      status?: AssetStatus;
      projectId?: string;
      expenseId?: string;
      purchaseDate?: string;
      purchaseCost?: number;
      currency?: string;
      currentValue?: number;
      depreciationMethodNotes?: string;
      maintenanceNotes?: string;
      updatedById?: string;
    },
  ) {}
}
