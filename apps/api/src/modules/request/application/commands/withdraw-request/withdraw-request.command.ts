import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';

export class WithdrawRequestCommand {
  constructor(
    public readonly id: string,
    public readonly note: string | undefined,
    public readonly user: AuthUser,
  ) {}
}
