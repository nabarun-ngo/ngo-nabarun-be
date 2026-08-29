export class CreateUserCommand {
  constructor(
    public readonly params: {
      email: string;
      firstName: string;
      lastName: string;
      title?: string;
      middleName?: string;
      dateOfBirth?: Date;
      gender?: string;
      bloodGroup?: string;
      about?: string;
      picture?: string;
      isPublic?: boolean;
      /** App profile UUID of the admin performing the create. Never idpSub. */
      createdById: string;
    },
  ) {}
}
