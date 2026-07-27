export class SubmitPublicWorkflowFormCommand {
  constructor(
    public readonly workflowName: string,
    public readonly values: Record<string, unknown>,
  ) {}
}
