import { DomainEvent } from '@nabarun-ngo/nestjs-shared-core';

export type DonorMergedSnapshot = {
  readonly sourceDonorId: string;
  readonly targetDonorId: string;
};

export class DonorMergedEvent extends DomainEvent<DonorMergedSnapshot> {
  constructor(sourceDonorId: string, targetDonorId: string) {
    super(targetDonorId, { sourceDonorId, targetDonorId });
  }
}
