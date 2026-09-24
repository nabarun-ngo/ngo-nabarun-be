import { EarningDetailFilterDto } from '../../../presentation/dtos/earning.dto';

export class ListEarningsQuery {
  constructor(
    public readonly filter: EarningDetailFilterDto = {},
  ) {}
}
