import { GoalDetailFilterDto } from '../../dtos/goal.dto';

export class ListGoalsQuery {
  constructor(
    public readonly projectId: string,
    public readonly filter: GoalDetailFilterDto = {},
  ) {}
}
