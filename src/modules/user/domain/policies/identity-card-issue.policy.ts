import { User } from '../aggregates/user/user.aggregate';
import { UserStatus } from '../enums/user-status.enum';
import { IdentityCardNotIssuableError } from '../errors/user.errors';

export class IdentityCardIssuePolicy {
  static assertCanPrint(user: User): void {
    if (user.status !== UserStatus.ACTIVE) {
      throw new IdentityCardNotIssuableError(
        'An identity card can only be printed for an active member',
      );
    }
    if (!user.isProfileComplete) {
      throw new IdentityCardNotIssuableError(
        'Complete the member profile before printing an identity card',
      );
    }
  }
}
