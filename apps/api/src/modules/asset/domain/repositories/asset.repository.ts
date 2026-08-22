import { IRepository } from '@nabarun-ngo/nestjs-shared-core';
import { Asset, AssetFilter } from '../aggregates/asset/asset.aggregate';

export const IAssetRepository = Symbol('IAssetRepository');

export interface IAssetRepository extends IRepository<Asset, string, AssetFilter> {}
