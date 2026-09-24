export class ApproveReportCommand {
  constructor(
    public readonly params: {
      reportId: string;
      approvedById: string;
      userPermissions: string[];
    },
  ) {}
}
