import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { DonorType } from '../enums/donor-type.enum';

export class DonorGuestFieldPolicy {
  static assertGuestFields(type: DonorType, fullName?: string): void {
    if (type === DonorType.GUEST && (!fullName || fullName.trim().length === 0)) {
      throw new BusinessException('Full name is required for guest donors');
    }
  }

  static assertMemberHasNoGuestFields(
    type: DonorType,
    fields: { fullName?: string; email?: string; phoneCode?: string; phoneNumber?: string },
  ): void {
    if (type !== DonorType.MEMBER) return;
    const hasGuestField = fields.fullName || fields.email || fields.phoneCode || fields.phoneNumber;
    if (hasGuestField) {
      throw new BusinessException('Member donors cannot have guest contact fields');
    }
  }
}
