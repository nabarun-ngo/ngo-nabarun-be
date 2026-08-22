import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { DonorStatus } from '../enums/donor-status.enum';

const ALLOWED: Record<DonorStatus, DonorStatus[]> = {
  [DonorStatus.ACTIVE]: [DonorStatus.PAUSED, DonorStatus.WAIVED, DonorStatus.DELETED],
  [DonorStatus.PAUSED]: [DonorStatus.ACTIVE, DonorStatus.DELETED],
  [DonorStatus.WAIVED]: [DonorStatus.ACTIVE, DonorStatus.DELETED],
  [DonorStatus.DELETED]: [DonorStatus.ACTIVE],
};

export class DonorStatusTransitionPolicy {
  static assertCanTransition(from: DonorStatus, to: DonorStatus): void {
    if (!ALLOWED[from]?.includes(to)) {
      throw new BusinessException(`Cannot transition donor status from ${from} to ${to}`);
    }
  }

  static requiresStatusEndDate(status: DonorStatus): boolean {
    return status === DonorStatus.PAUSED || status === DonorStatus.WAIVED;
  }
}
