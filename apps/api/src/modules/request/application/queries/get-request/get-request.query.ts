import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';

export class GetRequestQuery {
  constructor(
    public readonly id: string,
    public readonly user?: AuthUser,
  ) {}
}
