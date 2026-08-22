import { IRepository } from '@nabarun-ngo/nestjs-shared-core';
import { User } from '../aggregates/user/user.aggregate';

export interface UserFilter {
  firstName?: string;
  lastName?: string;
  email?: string;
  status?: string;
  phoneNumber?: string;
  isPublic?: boolean;
}

/**
 * Dashboard overview aggregates from one SQL round-trip (four scalar subselects):
 * finance SUMs + request inbox COUNT.
 */
export interface UserOverviewAggregates {
  pendingDonations: number;
  walletBalance: number;
  unsettledExpense: number;
  pendingTask: number;
}

/**
 * Token naming rule: Symbol name = interface name (DDD rule).
 * One import serves as both @Inject() token and TypeScript type.
 */
export const IUserRepository = Symbol('IUserRepository');

export interface IUserRepository extends IRepository<User, string, UserFilter> {
  /** Find by email — includes soft-deleted rows (for create-flow collision check). */
  findByEmail(email: string): Promise<User | null>;

  /** Find by IdP sub — active rows only (for JWT enrichment + cache miss). */
  findByIdPSub(sub: string): Promise<User | null>;

  /** Batch fetch by app-profile UUIDs — single query, avoids N+1. */
  findByIds(ids: string[]): Promise<User[]>;

  /** Batch fetch by IdP subjects — single query, avoids N+1. */
  findByIdPSubs(subs: string[]): Promise<User[]>;

  /**
   * One raw SQL round-trip: pending donations (via linked donor), wallet balance,
   * unsettled expenses, and request inbox count (role/group/permission eligibility).
   */
  getMyOverviewAggregates(
    userId: string,
    actorRoles: string[],
    actorGroups: string[],
    actorPermissions: string[],
  ): Promise<UserOverviewAggregates>;

  /**
   * Projection update: set denormalized roleKeys without bumping optimistic version.
   * No-ops when no active profile exists for the IdP subject.
   */
  updateRoleKeysByIdPSub(idpSub: string, roleKeys: string[]): Promise<void>;
}
