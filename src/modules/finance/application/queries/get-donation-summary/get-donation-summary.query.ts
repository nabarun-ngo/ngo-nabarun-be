export class GetDonationSummaryQuery {
  constructor(
    public readonly params: {
      donorId?: string;
      userProfileId?: string;
    },
  ) {}
}
