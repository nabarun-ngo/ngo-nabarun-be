import { DomainEvent } from '@ssdev-toolkit/nestjs-core';

export type DonorStatusChangedSnapshot = {
  readonly donorId: string;
  readonly previousStatus: string;
  readonly newStatus: string;
};

export class DonorStatusChangedEvent extends DomainEvent<DonorStatusChangedSnapshot> {
  constructor(donorId: string, previousStatus: string, newStatus: string) {
    super(donorId, { donorId, previousStatus, newStatus });
  }
}
