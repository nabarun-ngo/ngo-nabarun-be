import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { PrismaClient } from '../../prisma/client';
import type {
  AssetWhereInput,
  AssetWhereUniqueInput,
  AssetUncheckedCreateInput,
  AssetUncheckedUpdateInput,
  AssetOrderByWithRelationInput,
} from '../../prisma/models/Asset';
import { Asset, AssetFilter } from '../../../../modules/asset/domain/aggregates/asset/asset.aggregate';
import { IAssetRepository } from '../../../../modules/asset/domain/repositories/asset.repository';
import { AssetPrismaMapper, AssetRow } from '../mapper/asset-prisma.mapper';
import { MapperUtils } from '../../finance/mapper/mapper-utils';

const CUSTODY_HISTORY_INCLUDE = {
  custodyHistory: { orderBy: { assignedAt: 'desc' } },
} as const;

@Injectable()
export class AssetPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'asset',
    Asset,
    string,
    AssetFilter,
    AssetRow,
    AssetWhereInput,
    AssetWhereUniqueInput,
    AssetUncheckedCreateInput,
    AssetUncheckedUpdateInput,
    AssetOrderByWithRelationInput
  >
  implements IAssetRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'asset');
  }

  protected toDomain(row: AssetRow): Asset {
    return AssetPrismaMapper.toDomain(row)!;
  }

  protected toCreateInput(entity: Asset): AssetUncheckedCreateInput {
    return AssetPrismaMapper.toCreate(entity);
  }

  protected toUpdateInput(_id: string, entity: Asset): AssetUncheckedUpdateInput {
    return AssetPrismaMapper.toUpdate(entity);
  }

  protected toUniqueWhere(id: string): AssetWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(props?: AssetFilter): AssetWhereInput {
    return {
      deletedAt: null,
      ...(props?.status ? { status: props.status } : {}),
      ...(props?.category ? { category: props.category } : {}),
      ...(props?.custodianUserId ? { custodianUserId: props.custodianUserId } : {}),
      ...(props?.projectId ? { projectId: props.projectId } : {}),
    };
  }

  protected override defaultOrderBy(): AssetOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected override defaultPageSize(): number {
    return 20;
  }

  override async findById(id: string): Promise<Asset | null> {
    const row = await this.delegate.findFirst({
      where: { id, deletedAt: null },
      include: CUSTODY_HISTORY_INCLUDE,
    });
    return AssetPrismaMapper.toDomain(row);
  }

  override async create(entity: Asset): Promise<Asset> {
    const row = await this.delegate.create({
      data: AssetPrismaMapper.toCreate(entity),
      include: CUSTODY_HISTORY_INCLUDE,
    });
    return AssetPrismaMapper.toDomain(row)!;
  }

  override async update(id: string, entity: Asset): Promise<Asset> {
    await this.$transaction(async (tx) => {
      await tx.asset.update({
        where: { id },
        data: AssetPrismaMapper.toUpdate(entity),
      });

      const existing = await tx.assetCustodyRecord.findMany({ where: { assetId: id } });
      const existingIds = new Set(existing.map((r) => r.id));
      const desired = entity.custodyHistory;

      for (const record of desired) {
        const data = {
          custodianUserId: record.custodianUserId,
          assignedAt: record.assignedAt,
          assignedById: MapperUtils.undefinedToNull(record.assignedById),
          returnedAt: MapperUtils.undefinedToNull(record.returnedAt),
          returnedById: MapperUtils.undefinedToNull(record.returnedById),
          notes: MapperUtils.undefinedToNull(record.notes),
        };
        if (existingIds.has(record.id)) {
          await tx.assetCustodyRecord.update({ where: { id: record.id }, data });
          existingIds.delete(record.id);
        } else {
          await tx.assetCustodyRecord.create({
            data: { id: record.id, assetId: id, ...data },
          });
        }
      }
    });

    return (await this.findById(id))!;
  }

  override async delete(id: string): Promise<void> {
    await this.delegate.update({
      where: { id },
      data: { deletedAt: new Date() },
    });
  }
}
