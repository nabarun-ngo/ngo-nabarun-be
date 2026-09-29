import { DomainEvent } from '@ssdev-toolkit/nestjs-core';

export type DonationPaidSnapshot = {
  readonly donationId: string;
  readonly donorId?: string;
  readonly amount: number;
};

export class DonationPaidEvent extends DomainEvent<DonationPaidSnapshot> {
  constructor(
    public readonly donationId: string,
    donorId: string | undefined,
    amount: number,
  ) {
    super(donationId, { donationId, donorId, amount });
  }
}
