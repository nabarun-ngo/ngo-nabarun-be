import { DomainEvent } from '@nabarun-ngo/nestjs-shared-core';

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
