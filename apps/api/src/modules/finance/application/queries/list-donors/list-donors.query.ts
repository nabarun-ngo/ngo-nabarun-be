import { DonorStatus } from '../../../domain/enums/donor-status.enum';
import { DonorType } from '../../../domain/enums/donor-type.enum';
import { SortOrder } from '@nabarun-ngo/nestjs-shared-core';

export class ListDonorsQuery {
  constructor(
    public readonly filter: {
      q?: string;
      type?: DonorType;
      status?: DonorStatus;
    } = {},
    public readonly pageIndex?: number,
    public readonly pageSize?: number,
    public readonly sortBy?: string,
    public readonly sortDir?: SortOrder,
  ) {}
}
