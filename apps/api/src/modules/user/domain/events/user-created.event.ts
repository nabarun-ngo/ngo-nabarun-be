import { DomainEvent } from '@nabarun-ngo/nestjs-shared-core';

export type UserCreatedSnapshot = {
  readonly userId: string;
  readonly email: string;
  readonly idpSub: string | undefined;
  /** Display title for welcome correspondence (optional). */
  readonly title: string | undefined;
  /**
   * Auth0 hosted set-password URL from the Management API ticket.
   * Completing this link marks `email_verified` when the ticket was created with
   * `mark_email_as_verified: true`.
   */
  readonly setPasswordUrl: string | undefined;
};

export class UserCreatedEvent extends DomainEvent<UserCreatedSnapshot> {
  constructor(
    public readonly userId: string,
    public readonly email: string,
    public readonly idpSub: string | undefined,
    public readonly title: string | undefined,
    public readonly setPasswordUrl: string | undefined,
  ) {
    super(userId, { userId, email, idpSub, title, setPasswordUrl });
  }
}
