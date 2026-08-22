import { AssetDetailFilterDto } from '../../dtos/asset.dto';

export class ListAssetsQuery {
  constructor(
    public readonly filter: AssetDetailFilterDto = {},
    public readonly pageIndex?: number,
    public readonly pageSize?: number,
  ) {}
}
