import { Prisma } from '../../prisma/client';
import {
  Asset,
  AssetCustodyRecordProps,
} from '../../../../modules/asset/domain/aggregates/asset/asset.aggregate';
import { AssetCategory, AssetStatus } from '../../../../modules/asset/domain/enums/asset.enum';
import { MapperUtils } from '../../finance/mapper/mapper-utils';

export type AssetPersistence = Prisma.AssetGetPayload<{
  include: { custodyHistory: true };
}>;

export type AssetRow = Prisma.AssetGetPayload<Record<string, never>>;
export type CustodyRow = Prisma.AssetCustodyRecordGetPayload<Record<string, never>>;

export class AssetPrismaMapper {
  static toDomain(row: AssetRow | AssetPersistence | null, custody?: CustodyRow[]): Asset | null {
    if (!row) return null;
    const history =
      ('custodyHistory' in row && Array.isArray(row.custodyHistory)
        ? row.custodyHistory
        : custody) ?? [];

    return new Asset(
      row.id,
      row.name,
      row.category as AssetCategory,
      row.status as AssetStatus,
      MapperUtils.nullToUndefined(row.serialNumber),
      MapperUtils.nullToUndefined(row.location),
      MapperUtils.nullToUndefined(row.custodianUserId),
      MapperUtils.nullToUndefined(row.projectId),
      MapperUtils.nullToUndefined(row.expenseId),
      MapperUtils.nullToUndefined(row.purchaseDate),
      row.purchaseCost != null ? Number(row.purchaseCost) : undefined,
      MapperUtils.nullToUndefined(row.currency),
      row.currentValue != null ? Number(row.currentValue) : undefined,
      MapperUtils.nullToUndefined(row.depreciationMethodNotes),
      MapperUtils.nullToUndefined(row.maintenanceNotes),
      MapperUtils.nullToUndefined(row.createdById),
      MapperUtils.nullToUndefined(row.updatedById),
      history.map(AssetPrismaMapper.custodyToDomain),
      row.createdAt,
      row.updatedAt,
    );
  }

  static custodyToDomain(row: CustodyRow): AssetCustodyRecordProps {
    return {
      id: row.id,
      custodianUserId: row.custodianUserId,
      assignedAt: row.assignedAt,
      assignedById: MapperUtils.nullToUndefined(row.assignedById),
      returnedAt: MapperUtils.nullToUndefined(row.returnedAt),
      returnedById: MapperUtils.nullToUndefined(row.returnedById),
      notes: MapperUtils.nullToUndefined(row.notes),
    };
  }

  static toCreate(domain: Asset): Prisma.AssetUncheckedCreateInput {
    return {
      id: domain.id,
      name: domain.name,
      category: domain.category,
      serialNumber: MapperUtils.undefinedToNull(domain.serialNumber),
      location: MapperUtils.undefinedToNull(domain.location),
      status: domain.status,
      custodianUserId: MapperUtils.undefinedToNull(domain.custodianUserId),
      projectId: MapperUtils.undefinedToNull(domain.projectId),
      expenseId: MapperUtils.undefinedToNull(domain.expenseId),
      purchaseDate: MapperUtils.undefinedToNull(domain.purchaseDate),
      purchaseCost: MapperUtils.undefinedToNull(domain.purchaseCost),
      currency: MapperUtils.undefinedToNull(domain.currency),
      currentValue: MapperUtils.undefinedToNull(domain.currentValue),
      depreciationMethodNotes: MapperUtils.undefinedToNull(domain.depreciationMethodNotes),
      maintenanceNotes: MapperUtils.undefinedToNull(domain.maintenanceNotes),
      createdById: MapperUtils.undefinedToNull(domain.createdById),
      updatedById: MapperUtils.undefinedToNull(domain.updatedById),
      version: 0,
      custodyHistory: {
        create: domain.custodyHistory.map((r) => ({
          id: r.id,
          custodianUserId: r.custodianUserId,
          assignedAt: r.assignedAt,
          assignedById: MapperUtils.undefinedToNull(r.assignedById),
          returnedAt: MapperUtils.undefinedToNull(r.returnedAt),
          returnedById: MapperUtils.undefinedToNull(r.returnedById),
          notes: MapperUtils.undefinedToNull(r.notes),
        })),
      },
    };
  }

  static toUpdate(domain: Asset): Prisma.AssetUncheckedUpdateInput {
    return {
      name: domain.name,
      category: domain.category,
      serialNumber: MapperUtils.undefinedToNull(domain.serialNumber),
      location: MapperUtils.undefinedToNull(domain.location),
      status: domain.status,
      custodianUserId: MapperUtils.undefinedToNull(domain.custodianUserId),
      projectId: MapperUtils.undefinedToNull(domain.projectId),
      expenseId: MapperUtils.undefinedToNull(domain.expenseId),
      purchaseDate: MapperUtils.undefinedToNull(domain.purchaseDate),
      purchaseCost: MapperUtils.undefinedToNull(domain.purchaseCost),
      currency: MapperUtils.undefinedToNull(domain.currency),
      currentValue: MapperUtils.undefinedToNull(domain.currentValue),
      depreciationMethodNotes: MapperUtils.undefinedToNull(domain.depreciationMethodNotes),
      maintenanceNotes: MapperUtils.undefinedToNull(domain.maintenanceNotes),
      updatedById: MapperUtils.undefinedToNull(domain.updatedById),
      updatedAt: new Date(),
    };
  }
}
