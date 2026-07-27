import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IUserRepository } from '../../../domain/repositories/user.repository';
import { UserResponseDto } from '../../dtos/user-response.dto';
import { UserResponseMapper } from '../../mappers/user-response.mapper';
import { GetUserByEmailQuery } from './get-user-by-email.query';

@QueryHandler(GetUserByEmailQuery)
@Injectable()
export class GetUserByEmailHandler
  implements IQueryHandler<GetUserByEmailQuery, UserResponseDto | null>
{
  constructor(
    @Inject(IUserRepository) private readonly repo: IUserRepository,
  ) {}

  async execute(query: GetUserByEmailQuery): Promise<UserResponseDto | null> {
    const email = query.email.trim();
    if (!email) {
      return null;
    }

    const user = await this.repo.findByEmail(email);
    if (!user) {
      return null;
    }

    return UserResponseMapper.toDto(user);
  }
}
