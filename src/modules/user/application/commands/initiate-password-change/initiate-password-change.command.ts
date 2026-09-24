export class InitiatePasswordChangeCommand {
  constructor(
    public readonly params: {
      /** Profile id of the user requesting the password change. */
      userId: string;
      /** App profile UUID of the requestor (for audit). */
      requestorId: string;
      /** Current password — verified via Auth0 password grant before issuing a ticket. */
      currentPassword: string;
      /**
       * Optional post-reset return URL supplied by the client. Honoured only when it
       * falls within the configured application origin; otherwise the default is used.
       */
      redirectUrl?: string;
    },
  ) {}
}
