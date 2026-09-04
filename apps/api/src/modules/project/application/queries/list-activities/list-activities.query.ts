import { ActivityDetailFilterDto } from '../../dtos/activity.dto';

export class ListActivitiesQuery {
  constructor(
    public readonly filter: ActivityDetailFilterDto = {},
  ) {}
}
