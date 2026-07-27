import { ActivityDetailFilterDto } from '../../dtos/activity.dto';

export class ListActivitiesQuery {
  constructor(
    public readonly filter: ActivityDetailFilterDto = {},
    public readonly pageIndex?: number,
    public readonly pageSize?: number,
  ) {}
}
