import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';
import { ListRequestsQueryDto } from '../../dtos/request.dto';

export class ListRequestsQuery {
  constructor(
    public readonly query: ListRequestsQueryDto,
    public readonly user: AuthUser,
  ) {}
}
