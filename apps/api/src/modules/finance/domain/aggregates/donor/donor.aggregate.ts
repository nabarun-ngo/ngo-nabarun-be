import { AggregateRoot, BusinessException, generateUniqueNDigitNumber } from '@nabarun-ngo/nestjs-shared-core';
import { DonorType } from '../../enums/donor-type.enum';
import { DonorStatus } from '../../enums/donor-status.enum';
import { DonorGuestFieldPolicy } from '../../policies/donor-guest-field.policy';
import { DonorStatusTransitionPolicy } from '../../policies/donor-status-transition.policy';
import { DonorCreatedEvent } from '../../events/donor-created.event';
import { DonorStatusChangedEvent } from '../../events/donor-status-changed.event';

export class Donor extends AggregateRoot<string> {
  #type: DonorType;
  #status: DonorStatus;
  #preferredAmount: number | undefined;
  #statusEndDate: Date | undefined;
  #fullName: string | undefined;
  #email: string | undefined;
  #phoneCode: string | undefined;
  #phoneNumber: string | undefined;
  #userProfileId: string | undefined;

  constructor(
    id: string,
    type: DonorType,
    status: DonorStatus,
    preferredAmount?: number,
    statusEndDate?: Date,
    fullName?: string,
    email?: string,
    phoneCode?: string,
    phoneNumber?: string,
    userProfileId?: string,
    createdAt?: Date,
    updatedAt?: Date,
  ) {
    super(id, createdAt, updatedAt);
    this.#type = type;
    this.#status = status;
    this.#preferredAmount = preferredAmount;
    this.#statusEndDate = statusEndDate;
    this.#fullName = fullName;
    this.#email = email;
    this.#phoneCode = phoneCode;
    this.#phoneNumber = phoneNumber;
    this.#userProfileId = userProfileId;
  }

  static createMember(props: { userProfileId: string; preferredAmount?: number }): Donor {
    if (!props.userProfileId) {
      throw new BusinessException('userProfileId is required for member donors');
    }
    const donor = new Donor(
      props.userProfileId,
      DonorType.MEMBER,
      DonorStatus.ACTIVE,
      props.preferredAmount,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      props.userProfileId,
    );
    donor.addDomainEvent(new DonorCreatedEvent(donor.id, donor.type, donor.userProfileId));
    return donor;
  }

  static createGuest(props: {
    fullName: string;
    email?: string;
    phoneCode?: string;
    phoneNumber?: string;
    preferredAmount?: number;
  }): Donor {
    DonorGuestFieldPolicy.assertGuestFields(DonorType.GUEST, props.fullName);
    const donor = new Donor(
      `NDNR${generateUniqueNDigitNumber(8)}`,
      DonorType.GUEST,
      DonorStatus.ACTIVE,
      props.preferredAmount,
      undefined,
      props.fullName.trim(),
      props.email?.trim() || undefined,
      props.phoneCode,
      props.phoneNumber,
      undefined,
    );
    donor.addDomainEvent(new DonorCreatedEvent(donor.id, donor.type));
    return donor;
  }

  pause(endDate: Date): void {
    this.transitionTo(DonorStatus.PAUSED, endDate);
  }

  waive(endDate: Date): void {
    this.transitionTo(DonorStatus.WAIVED, endDate);
  }

  activate(): void {
    this.transitionTo(DonorStatus.ACTIVE);
    this.#statusEndDate = undefined;
  }

  markDeleted(): void {
    this.transitionTo(DonorStatus.DELETED);
    this.#statusEndDate = undefined;
  }

  restore(): void {
    this.transitionTo(DonorStatus.ACTIVE);
    this.#statusEndDate = undefined;
  }

  updateGuestDetails(props: {
    fullName?: string;
    email?: string;
    phoneCode?: string;
    phoneNumber?: string;
  }): void {
    if (this.#type !== DonorType.GUEST) {
      throw new BusinessException('Only guest donors can update guest contact details');
    }
    if (this.#status === DonorStatus.DELETED) {
      throw new BusinessException('Cannot update a deleted donor');
    }
    if (props.fullName !== undefined) {
      DonorGuestFieldPolicy.assertGuestFields(DonorType.GUEST, props.fullName);
      this.#fullName = props.fullName.trim();
    }
    if (props.email !== undefined) {
      this.#email = props.email.trim() || undefined;
    }
    if (props.phoneCode !== undefined) {
      this.#phoneCode = props.phoneCode || undefined;
    }
    if (props.phoneNumber !== undefined) {
      this.#phoneNumber = props.phoneNumber || undefined;
    }
    this.touch();
  }

  updatePreferredAmount(amount: number): void {
    if (amount <= 0) {
      throw new BusinessException('Preferred amount must be greater than zero');
    }
    this.#preferredAmount = amount;
    this.touch();
  }

  updateMemberSchedule(props: {
    status?: DonorStatus;
    preferredAmount?: number;
    statusEndDate?: Date;
  }): void {
    if (this.#type !== DonorType.MEMBER) {
      throw new BusinessException('Only member donors can be updated via member schedule');
    }
    if (props.preferredAmount !== undefined) {
      this.updatePreferredAmount(props.preferredAmount);
    }
    if (props.status !== undefined) {
      if (props.status === DonorStatus.PAUSED || props.status === DonorStatus.WAIVED) {
        if (!props.statusEndDate) {
          throw new BusinessException('statusEndDate is required when pausing or waiving a donor');
        }
        this.transitionTo(props.status, props.statusEndDate);
      } else {
        this.transitionTo(props.status);
        if (props.status === DonorStatus.ACTIVE) {
          this.#statusEndDate = undefined;
        }
      }
    }
  }

  absorbGuestContactFrom(source: Donor): void {
    if (this.#type !== DonorType.GUEST || source.type !== DonorType.GUEST) {
      throw new BusinessException('Can only merge guest donor contact fields');
    }
    if (!this.#email && source.email) {
      this.#email = source.email;
    }
    if (!this.#phoneNumber && source.phoneNumber) {
      this.#phoneNumber = source.phoneNumber;
      this.#phoneCode = source.phoneCode;
    }
    this.touch();
  }

  shouldRaiseDonation(): boolean {
    return this.#status === DonorStatus.ACTIVE || this.#status === DonorStatus.PAUSED;
  }

  shouldNotifyOnRaise(): boolean {
    return this.#status === DonorStatus.ACTIVE;
  }

  isWaivedOrDeleted(): boolean {
    return this.#status === DonorStatus.WAIVED || this.#status === DonorStatus.DELETED;
  }

  get type(): DonorType { return this.#type; }
  get status(): DonorStatus { return this.#status; }
  get preferredAmount(): number | undefined { return this.#preferredAmount; }
  get statusEndDate(): Date | undefined { return this.#statusEndDate; }
  get fullName(): string | undefined { return this.#fullName; }
  get email(): string | undefined { return this.#email; }
  get phoneCode(): string | undefined { return this.#phoneCode; }
  get phoneNumber(): string | undefined { return this.#phoneNumber; }
  get userProfileId(): string | undefined { return this.#userProfileId; }

  private transitionTo(status: DonorStatus, statusEndDate?: Date): void {
    DonorStatusTransitionPolicy.assertCanTransition(this.#status, status);
    if (DonorStatusTransitionPolicy.requiresStatusEndDate(status) && !statusEndDate) {
      throw new BusinessException(`statusEndDate is required when setting status to ${status}`);
    }
    const previous = this.#status;
    this.#status = status;
    this.#statusEndDate = DonorStatusTransitionPolicy.requiresStatusEndDate(status) ? statusEndDate : undefined;
    this.touch();
    this.addDomainEvent(new DonorStatusChangedEvent(this.id, previous, status));
  }
}
