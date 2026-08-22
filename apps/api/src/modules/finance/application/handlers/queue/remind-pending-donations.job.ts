export class RemindPendingDonationsJob {
  constructor(public readonly payload: { donorId?: string } = {}) {}
}
