import type { AuthUser } from '@ssdev-toolkit/nestjs-auth';
import { CreateRequestDto } from '../../dtos/request.dto';

export class CreateRequestCommand {
  constructor(
    public readonly dto: CreateRequestDto,
    public readonly user: AuthUser,
  ) {}
}
