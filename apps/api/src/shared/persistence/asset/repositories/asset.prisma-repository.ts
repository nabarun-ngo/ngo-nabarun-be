import { Injectable } from '@nestjs/common';
import { BaseFilter, Page } from '@nabarun-ngo/nestjs-shared-core';
import { BasePrismaService } from '@nabarun-ngo/nestjs-shared-persistence';
import { Prisma, PrismaClient } from '../../prisma/client';
import { Asset, AssetFilter } from '../../../../modules/asset/domain/aggregates/asset/asset.aggregate';
import { IAssetRepository } from '../../../../modules/asset/domain/repositories/asset.repository';
import { AssetPrismaMapper } from '../mapper/asset-prisma.mapper';
import { MapperUtils } from '../../finance/mapper/mapper-utils';

@Injectable()
export class AssetPrismaRepository implements IAssetRepository {
  constructor(private readonly database: BasePrismaService<PrismaClient>) {}

  async count(filter: AssetFilter): Promise<number> {
    return this.database.client.asset.count({ where: this.where(filter) });
  }

  async findPaged(filter?: BaseFilter<AssetFilter>): Promise<Page<Asset>> {
    const where = this.where(filter?.props);
    const pageIndex = filter?.pageIndex ?? 0;
    const pageSize = filter?.pageSize ?? 20;
    const [rows, total] = await Promise.all([
      this.database.client.asset.findMany({
        where,
        orderBy: { createdAt: 'desc' },
        skip: pageIndex * pageSize,
        take: pageSize,
      }),
      this.database.client.asset.count({ where }),
    ]);
    return new Page(
      rows.map((r) => AssetPrismaMapper.toDomain(r)!),
      total,
      pageIndex,
      pageSize,
    );
  }

  async findAll(filter?: AssetFilter): Promise<Asset[]> {
    const rows = await this.database.client.asset.findMany({
      where: this.where(filter),
      orderBy: { createdAt: 'desc' },
    });
    return rows.map((r) => AssetPrismaMapper.toDomain(r)!);
  }

  async findById(id: string): Promise<Asset | null> {
    const row = await this.database.client.asset.findFirst({
      where: { id, deletedAt: null },
      include: { custodyHistory: { orderBy: { assignedAt: 'desc' } } },
    });
    return AssetPrismaMapper.toDomain(row);
  }

  async create(entity: Asset): Promise<Asset> {
    const row = await this.database.client.asset.create({
      data: AssetPrismaMapper.toCreate(entity),
      include: { custodyHistory: { orderBy: { assignedAt: 'desc' } } },
    });
    return AssetPrismaMapper.toDomain(row)!;
  }

  async update(id: string, entity: Asset): Promise<Asset> {
    await this.database.client.$transaction(async (tx) => {
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

  async delete(id: string): Promise<void> {
    await this.database.client.asset.update({
      where: { id },
      data: { deletedAt: new Date() },
    });
  }

  private where(props?: AssetFilter): Prisma.AssetWhereInput {
    return {
      deletedAt: null,
      ...(props?.status ? { status: props.status } : {}),
      ...(props?.category ? { category: props.category } : {}),
      ...(props?.custodianUserId ? { custodianUserId: props.custodianUserId } : {}),
      ...(props?.projectId ? { projectId: props.projectId } : {}),
    };
  }
}
