export class SubmitDynamicPublicFormCommand {
  constructor(
    public readonly publicFormKey: string,
    public readonly values: Record<string, unknown>,
  ) {}
}
