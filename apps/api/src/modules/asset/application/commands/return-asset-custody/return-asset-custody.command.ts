export class ReturnAssetCustodyCommand {
  constructor(
    public readonly params: {
      id: string;
      notes?: string;
      returnedById?: string;
    },
  ) {}
}
