export class GetWorkflowDefinitionQuery {
  constructor(
    public readonly definitionId: string,
    public readonly version?: number,
  ) {}
}
