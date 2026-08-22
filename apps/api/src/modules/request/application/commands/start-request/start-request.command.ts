import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';

export class StartRequestCommand {
  constructor(
    public readonly id: string,
    public readonly user: AuthUser,
  ) {}
}
