export class DeleteReportCommand {
  constructor(
    public readonly reportId: string,
    public readonly userId: string,
    public readonly userPermissions: string[],
  ) {}
}
