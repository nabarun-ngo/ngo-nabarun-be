export class CreateGuestDonorCommand {
  constructor(
    public readonly params: {
      fullName: string;
      email?: string;
      phoneCode?: string;
      phoneNumber?: string;
      preferredAmount?: number;
    },
  ) {}
}
