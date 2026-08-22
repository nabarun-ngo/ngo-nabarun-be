export class TriggerMonthlyDonationJob {
  constructor(public readonly payload: { donorId?: string } = {}) {}
}
