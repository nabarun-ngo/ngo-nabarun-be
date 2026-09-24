import type { AuthUser } from '@ssdev-toolkit/nestjs-auth';
import { ListRequestsQueryDto } from '../../dtos/request.dto';

export class ListRequestsQuery {
  constructor(
    public readonly query: ListRequestsQueryDto,
    public readonly user: AuthUser,
  ) {}
}
