export class GetMyInboxQuery {
  constructor(
    public readonly userId: string,
    public readonly userPermissions: string[] = [],
  ) {}
}
