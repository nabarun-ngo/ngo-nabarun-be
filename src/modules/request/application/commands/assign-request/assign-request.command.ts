import type { AuthUser } from '@ssdev-toolkit/nestjs-auth';

export class AssignRequestCommand {
  constructor(
    public readonly id: string,
    public readonly assigneeId: string,
    public readonly user: AuthUser,
  ) {}
}
