export class UpdateGuestDonorCommand {
  constructor(
    public readonly params: {
      donorId: string;
      fullName?: string;
      email?: string;
      phoneCode?: string;
      phoneNumber?: string;
    },
  ) {}
}
