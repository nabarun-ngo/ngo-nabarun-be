export class RegenerateReportCommand {
  constructor(
    public readonly params: {
      reportId: string;
      requestedById: string;
      userPermissions: string[];
    },
  ) {}
}
