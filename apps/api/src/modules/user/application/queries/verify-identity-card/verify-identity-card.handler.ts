import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IUserRepository } from '../../../domain/repositories/user.repository';
import { UniqueMemberId } from '../../../domain/value-objects/unique-member-id.vo';
import { UserStatus } from '../../../domain/enums/user-status.enum';
import {
  IdentityCardVerificationDto,
  IdentityCardVerificationOutcome,
} from '../../dtos/identity-card.dto';
import { VerifyIdentityCardQuery } from './verify-identity-card.query';

@QueryHandler(VerifyIdentityCardQuery)
@Injectable()
export class VerifyIdentityCardHandler
  implements IQueryHandler<VerifyIdentityCardQuery, IdentityCardVerificationDto>
{
  constructor(
    @Inject(IUserRepository) private readonly users: IUserRepository,
  ) {}

  async execute(query: VerifyIdentityCardQuery): Promise<IdentityCardVerificationDto> {
    const uniqueMemberId = query.uniqueMemberId?.trim() ?? '';
    if (!UniqueMemberId.isWellFormed(uniqueMemberId)) {
      return { outcome: IdentityCardVerificationOutcome.UNKNOWN };
    }

    const user = await this.users.findByUniqueMemberId(uniqueMemberId);
    if (!user) {
      return { outcome: IdentityCardVerificationOutcome.UNKNOWN };
    }

    const dto = new IdentityCardVerificationDto();
    dto.uniqueMemberId = user.uniqueMemberId;
    dto.displayName = user.fullName;
    dto.outcome =
      user.status === UserStatus.ACTIVE
        ? IdentityCardVerificationOutcome.VALID
        : IdentityCardVerificationOutcome.INVALID;
    return dto;
  }
}
