import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IUserRepository } from '../../../domain/repositories/user.repository';
import { User } from '../../../domain/aggregates/user/user.aggregate';
import { UserNotFoundError } from '../../../domain/errors/user.errors';
import { UserProfileCompletenessPolicy } from '../../../domain/policies/user-profile-completeness.policy';
import { UserResponseDto } from '../../dtos/user-response.dto';
import { UserResponseMapper } from '../../mappers/user-response.mapper';
import { GetMyProfileQuery } from './get-my-profile.query';

@QueryHandler(GetMyProfileQuery)
@Injectable()
export class GetMyProfileHandler
  implements IQueryHandler<GetMyProfileQuery, UserResponseDto>
{
  constructor(
    @Inject(IUserRepository) private readonly repo: IUserRepository,
  ) {}

  async execute(query: GetMyProfileQuery): Promise<UserResponseDto> {
    const user = await this.resolveUser(query);
    if (!user) {
      throw new UserNotFoundError(
        query.criteria.userId ?? query.criteria.idpSub ?? 'unknown',
      );
    }

    const dto = UserResponseMapper.toDto(user);
    dto.missingFields = UserProfileCompletenessPolicy.missingFields(user);
    return dto;
  }

  private async resolveUser(query: GetMyProfileQuery): Promise<User | null> {
    const { userId, idpSub } = query.criteria;

    if (userId) {
      const byId = await this.repo.findById(userId);
      if (byId) {
        return byId;
      }
    }

    if (idpSub) {
      return this.repo.findByIdPSub(idpSub);
    }

    return null;
  }
}
