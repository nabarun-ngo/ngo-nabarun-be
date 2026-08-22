import { AssetCategory, AssetStatus } from '../../../domain/enums/asset.enum';

export class CreateAssetCommand {
  constructor(
    public readonly params: {
      name: string;
      category: AssetCategory;
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
      createdById?: string;
    },
  ) {}
}
