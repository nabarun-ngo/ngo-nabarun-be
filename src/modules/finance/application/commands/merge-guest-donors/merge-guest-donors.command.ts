export class MergeGuestDonorsCommand {
  constructor(
    public readonly params: {
      sourceDonorId: string;
      targetDonorId: string;
    },
  ) {}
}
