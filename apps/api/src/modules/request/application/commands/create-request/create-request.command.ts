import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';
import { CreateRequestDto } from '../../dtos/request.dto';

export class CreateRequestCommand {
  constructor(
    public readonly dto: CreateRequestDto,
    public readonly user: AuthUser,
  ) {}
}
