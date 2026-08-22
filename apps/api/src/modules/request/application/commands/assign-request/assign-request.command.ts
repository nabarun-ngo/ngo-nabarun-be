import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';

export class AssignRequestCommand {
  constructor(
    public readonly id: string,
    public readonly assigneeId: string,
    public readonly user: AuthUser,
  ) {}
}
