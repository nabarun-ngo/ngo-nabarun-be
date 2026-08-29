export class IssueIdentityCardCommand {
  constructor(
    public readonly params: {
      userId: string;
      pictureDataUrl?: string;
    },
  ) {}
}
