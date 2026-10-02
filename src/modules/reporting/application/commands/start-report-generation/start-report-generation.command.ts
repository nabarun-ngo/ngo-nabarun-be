export class StartReportGenerationCommand {
  constructor(
    public readonly params: {
      reportCode: string;
      parameters: Record<string, unknown>;
      requestedById: string;
      userPermissions: string[];
      userRoles: string[];
    },
  ) {}
}
