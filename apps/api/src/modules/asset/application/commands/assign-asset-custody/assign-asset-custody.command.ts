export class AssignAssetCustodyCommand {
  constructor(
    public readonly params: {
      id: string;
      custodianUserId: string;
      notes?: string;
      assignedById?: string;
    },
  ) {}
}
