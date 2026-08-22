import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IUserRepository } from '../../../domain/repositories/user.repository';
import { UserOverviewMetricsDto } from '../../dtos/user-overview-metrics.dto';
import { GetMyOverviewMetricsQuery } from './get-my-overview-metrics.query';

const DONATION_READ_PERMISSIONS = [
  'read:donations',
  'read:donation_guest',
  'read:member_donations',
  'update:donation',
  'create:donation',
] as const;

function hasAnyPermission(permissions: string[], required: readonly string[]): boolean {
  return required.some((perm) => permissions.includes(perm));
}

@QueryHandler(GetMyOverviewMetricsQuery)
@Injectable()
export class GetMyOverviewMetricsHandler
  implements IQueryHandler<GetMyOverviewMetricsQuery, UserOverviewMetricsDto>
{
  constructor(
    @Inject(IUserRepository) private readonly userRepo: IUserRepository,
  ) { }

  async execute(query: GetMyOverviewMetricsQuery): Promise<UserOverviewMetricsDto> {
    const { userId, permissions, userRoles, roleGroups } = query;
    const result: UserOverviewMetricsDto = {};

    const needsFinance = hasAnyPermission(permissions, [
      ...DONATION_READ_PERMISSIONS,
      'read:users',
      'read:expenses',
    ]);
    const needsInbox = permissions.includes('read:requests');

    if (!needsFinance && !needsInbox) {
      return result;
    }

    // One SQL round-trip (four scalar subselects); strip fields by permission below.
    const aggregates = await this.userRepo.getMyOverviewAggregates(
      userId,
      userRoles,
      roleGroups,
      permissions,
    );

    if (needsFinance && hasAnyPermission(permissions, DONATION_READ_PERMISSIONS)) {
      result.pendingDonations = aggregates.pendingDonations;
    }

    if (needsFinance && permissions.includes('read:users')) {
      result.walletBalance = aggregates.walletBalance;
    }

    if (needsFinance && permissions.includes('read:expenses')) {
      result.unsettledExpense = aggregates.unsettledExpense;
    }

    if (needsInbox) {
      result.pendingTask = aggregates.pendingTask;
    }

    return result;
  }
}
