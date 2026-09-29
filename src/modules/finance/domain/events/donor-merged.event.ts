import { DomainEvent } from '@ssdev-toolkit/nestjs-core';

export type DonorMergedSnapshot = {
  readonly sourceDonorId: string;
  readonly targetDonorId: string;
};

export class DonorMergedEvent extends DomainEvent<DonorMergedSnapshot> {
  constructor(sourceDonorId: string, targetDonorId: string) {
    super(targetDonorId, { sourceDonorId, targetDonorId });
  }
}
