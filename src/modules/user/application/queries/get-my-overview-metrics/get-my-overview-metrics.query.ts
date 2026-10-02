export class GetMyOverviewMetricsQuery {
  constructor(
    public readonly userId: string,
    public readonly permissions: string[] = [],
    public readonly userRoles: string[] = [],
    public readonly roleGroups: string[] = [],
  ) { }
}
