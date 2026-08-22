import { DomainEvent } from '@nabarun-ngo/nestjs-shared-core';

export type DonorCreatedSnapshot = {
  readonly donorId: string;
  readonly type: string;
  readonly userProfileId?: string;
};

export class DonorCreatedEvent extends DomainEvent<DonorCreatedSnapshot> {
  constructor(donorId: string, type: string, userProfileId?: string) {
    super(donorId, { donorId, type, userProfileId });
  }
}
