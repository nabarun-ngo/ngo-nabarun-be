import type { AuthUser } from '@ssdev-toolkit/nestjs-auth';

export class StartRequestCommand {
  constructor(
    public readonly id: string,
    public readonly user: AuthUser,
  ) {}
}
