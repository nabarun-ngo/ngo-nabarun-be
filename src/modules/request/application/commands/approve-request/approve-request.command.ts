import type { AuthUser } from '@ssdev-toolkit/nestjs-auth';

export class ApproveRequestCommand {
  constructor(
    public readonly id: string,
    public readonly note: string | undefined,
    public readonly user: AuthUser,
  ) {}
}
