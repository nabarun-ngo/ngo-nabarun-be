import { EarningDetailFilterDto } from '../../../presentation/dtos/earning.dto';

export class ListEarningsQuery {
  constructor(
    public readonly filter: EarningDetailFilterDto = {},
    public readonly pageIndex?: number,
    public readonly pageSize?: number,
  ) {}
}

