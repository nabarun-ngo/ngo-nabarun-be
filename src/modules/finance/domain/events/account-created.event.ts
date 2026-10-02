import { DomainEvent } from '@ssdev-toolkit/nestjs-core';

export type AccountCreatedSnapshot = {
  readonly accountId: string;
  readonly name: string;
  readonly type: string;
};

export class AccountCreatedEvent extends DomainEvent<AccountCreatedSnapshot> {
  constructor(accountId: string, name: string, type: string) {
    super(accountId, { accountId, name, type });
  }
}
