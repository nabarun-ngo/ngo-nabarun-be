import { AccountType } from '../../enums/account-type.enum';
import { AccountOwnerType } from '../../enums/account-owner-type.enum';
import { AccountStatus } from '../../enums/account-status.enum';
import { BankDetail } from '../../value-objects/bank-detail.vo';
import { UPIDetail, getPrimaryUpiDetail, normalizeUpiDetails } from '../../value-objects/upi-detail.vo';
import { ACCOUNT_TYPE_CONFIG } from '../../config/account-type.config';
import { AggregateRoot, BusinessException, generateUniqueNDigitNumber } from '@nabarun-ngo/nestjs-shared-core';
import { AccountCreatedEvent } from '../../events/account-created.event';
import { Transaction } from '../../entities/transaction.entity';
import { TransactionRefType, TransactionType } from '../../enums/transaction.enum';
import { TransactionCreatedEvent } from '../../events/transaction-created.event';

class TxnDetail {
  transactionRef: string;
  referenceId?: string;
  referenceType?: TransactionRefType;
  description: string;
  txnDate: Date;
  refAccountId?: string;
}

/**
 * Account Domain Model (Aggregate Root)
 * Represents a financial account in the system
 * All business logic and validations are in this domain model
 */
export class Account extends AggregateRoot<string> {
  #name: string;
  #type: AccountType;
  #ownerType: AccountOwnerType;
  #currency: string;
  #status: AccountStatus;
  #description: string | undefined;
  #accountHolderName: string | undefined;
  #accountHolderId: string | undefined;
  #custodianUserIds: string[];
  #activatedOn: Date | undefined;
  #bankDetail: BankDetail | undefined;
  #upiDetails: UPIDetail[] | undefined;
  #transactions: Transaction[] = [];

  constructor(
    id: string,
    name: string,
    type: AccountType,
    ownerType: AccountOwnerType,
    currency: string,
    status: AccountStatus,
    description: string | undefined,
    transactions: Transaction[] = [],
    accountHolderName?: string,
    accountHolderId?: string,
    custodianUserIds: string[] = [],
    activatedOn?: Date,
    bankDetail?: BankDetail,
    upiDetails?: UPIDetail[],
    createdAt?: Date,
    updatedAt?: Date,
  ) {
    super(id, createdAt, updatedAt);
    this.#name = name;
    this.#type = type;
    this.#ownerType = ownerType;
    this.#currency = currency;
    this.#status = status;
    this.#description = description;
    this.#accountHolderName = accountHolderName;
    this.#accountHolderId = accountHolderId;
    this.#custodianUserIds = custodianUserIds.length ? [...new Set(custodianUserIds)] : [];
    this.#activatedOn = activatedOn;
    this.#bankDetail = bankDetail;
    this.#upiDetails = upiDetails;
    this.#transactions = transactions;
  }

  static create(props: {
    name: string;
    type: AccountType;
    ownerType: AccountOwnerType;
    currency: string;
    initialBalance?: number;
    description?: string;
    accountHolderId?: string;
    accountHolderName?: string;
    custodianUserIds?: string[];
    bankDetail?: BankDetail;
    upiDetails?: UPIDetail[];
  }): Account {
    if (!props.name || props.name.trim().length === 0) {
      throw new BusinessException('Account name is required');
    }
    if (!props.currency || props.currency.trim().length === 0) {
      throw new BusinessException('Currency is required');
    }
    if (props.initialBalance !== undefined && props.initialBalance < 0) {
      throw new BusinessException('Initial balance cannot be negative');
    }
    if (!ACCOUNT_TYPE_CONFIG[props.type]) {
      throw new BusinessException('Invalid account type');
    }
    if (!ACCOUNT_TYPE_CONFIG[props.type].allowedOwnerTypes.includes(props.ownerType)) {
      throw new BusinessException('Owner type is not allowed for this account type');
    }
    if (props.ownerType === AccountOwnerType.ORG) {
      if (props.accountHolderId) {
        throw new BusinessException('Org accounts cannot have an account holder');
      }
    } else if (!props.accountHolderId) {
      throw new BusinessException('Individual accounts require an account holder');
    }

    const now = new Date();
    const account = new Account(
      `NACC${generateUniqueNDigitNumber(8)}`,
      props.name,
      props.type,
      props.ownerType,
      props.currency,
      AccountStatus.ACTIVE,
      props.description,
      [],
      props.accountHolderName,
      props.ownerType === AccountOwnerType.ORG ? undefined : props.accountHolderId,
      props.custodianUserIds ?? [],
      now,
      props.bankDetail,
      normalizeUpiDetails(props.upiDetails),
      now,
      now,
    );
    account.validateTypeDetails();
    if (props.initialBalance) {
      account.credit(props.initialBalance, {
        transactionRef: `TXR${generateUniqueNDigitNumber(10)}`,
        description: 'Initial balance',
        txnDate: now,
      });
    }
    account.addDomainEvent(new AccountCreatedEvent(account.id, account.name, account.type));
    return account;
  }

  credit(amount: number, txnDetail: TxnDetail): void {
    if (amount <= 0) {
      throw new BusinessException('Credit amount must be positive');
    }

    if (this.#status !== AccountStatus.ACTIVE) {
      throw new BusinessException('Cannot credit to inactive or blocked account');
    }

    const transaction = Transaction.createIn({
      txnRef: txnDetail.transactionRef,
      amount: amount,
      currency: this.#currency,
      accountId: this.id,
      txnParticulars: `Credit ${this.#currency} ${amount} to account ${this.id} for ${txnDetail.description}`,
      referenceId: txnDetail.referenceId,
      referenceType: txnDetail.referenceType,
      description: txnDetail.description,
      transactionDate: txnDetail.txnDate,
      sourceAccountId: txnDetail.refAccountId,
    });

    this.#transactions.push(transaction);
    this.addDomainEvent(new TransactionCreatedEvent(
      transaction.id,
      transaction.transactionRef,
      transaction.accountId,
      transaction.amount,
      transaction.type,
    ));
    this.touch();
  }

  debit(amount: number, txnDetail: TxnDetail): void {
    if (amount <= 0) {
      throw new BusinessException('Debit amount must be positive');
    }

    if (this.#status !== AccountStatus.ACTIVE) {
      throw new BusinessException('Cannot debit from inactive or blocked account');
    }

    if (!this.hasSufficientFunds(amount)) {
      throw new BusinessException('You dont have sufficiend balance.');
    }

    const transaction = Transaction.createOut({
      txnRef: txnDetail.transactionRef,
      amount: amount,
      currency: this.#currency,
      accountId: this.id,
      txnParticulars: `Debit ${this.#currency} ${amount} from account ${this.id} for ${txnDetail.description}`,
      referenceId: txnDetail.referenceId,
      referenceType: txnDetail.referenceType,
      description: txnDetail.description,
      transactionDate: txnDetail.txnDate,
      destAccountId: txnDetail.refAccountId,
    });

    this.#transactions.push(transaction);
    this.addDomainEvent(new TransactionCreatedEvent(
      transaction.id,
      transaction.transactionRef,
      transaction.accountId,
      transaction.amount,
      transaction.type,
    ));
    this.touch();
  }

  close(): void {
    if (this.#status === AccountStatus.CLOSED) {
      throw new BusinessException('Cannot close a already closed account');
    }
    if (this.balance !== 0) {
      throw new BusinessException('Cannot close an account with balance');
    }
    this.#status = AccountStatus.CLOSED;
    this.touch();
  }

  activate(): void {
    if (this.#status === AccountStatus.CLOSED) {
      throw new BusinessException('Cannot activate a closed account');
    }
    this.#status = AccountStatus.ACTIVE;
    if (!this.#activatedOn) {
      this.#activatedOn = new Date();
    }
    this.touch();
  }

  update(props: {
    name?: string;
    description?: string;
    bankDetail?: BankDetail;
    upiDetails?: UPIDetail[];
    accountHolderName?: string;
    custodianUserIds?: string[];
  }): void {
    if (props.name !== undefined) {
      if (!props.name || props.name.trim().length === 0) {
        throw new BusinessException('Account name cannot be empty');
      }
      this.#name = props.name;
    }
    if (props.description !== undefined) {
      this.#description = props.description;
    }
    if (props.bankDetail !== undefined) {
      this.#bankDetail = props.bankDetail;
    }
    if (props.upiDetails !== undefined) {
      this.#upiDetails = normalizeUpiDetails(props.upiDetails);
    }
    if (props.accountHolderName !== undefined) {
      this.#accountHolderName = props.accountHolderName;
    }
    if (props.custodianUserIds !== undefined) {
      this.#custodianUserIds = props.custodianUserIds.length
        ? [...new Set(props.custodianUserIds)]
        : [];
    }
    this.validateTypeDetails();
    this.touch();
  }

  private validateTypeDetails(): void {
    const config = ACCOUNT_TYPE_CONFIG[this.#type];

    if (config.requiredDetails.includes('bankDetail') && !this.#bankDetail) {
      throw new BusinessException('Bank detail is required for this account type');
    }

    if (this.#type === AccountType.BANK && this.#bankDetail && !this.#bankDetail.IFSCNumber?.trim()) {
      throw new BusinessException('IFSC is required for bank accounts');
    }

    if (this.#type === AccountType.INVESTMENT && !this.#description?.trim()) {
      throw new BusinessException('Description is required for investment accounts');
    }

    if (this.#upiDetails?.length && this.#type !== AccountType.BANK && this.#type !== AccountType.WALLET) {
      throw new BusinessException('UPI details are only allowed on bank and wallet accounts');
    }

    if (this.#custodianUserIds.length && this.#ownerType !== AccountOwnerType.ORG) {
      throw new BusinessException('Custodians are only allowed on organization accounts');
    }
  }

  get name(): string { return this.#name; }
  get type(): AccountType { return this.#type; }
  get ownerType(): AccountOwnerType { return this.#ownerType; }
  get balance(): number {
    return this.#transactions.reduce((acc, txn) => {
      if (txn.type === TransactionType.IN) {
        return acc + (txn.amount ?? 0);
      } else {
        return acc - (txn.amount ?? 0);
      }
    }, 0);
  }
  get currency(): string { return this.#currency; }
  get status(): AccountStatus { return this.#status; }
  get description(): string | undefined { return this.#description; }
  get accountHolderName(): string | undefined { return this.#accountHolderName; }
  get accountHolderId(): string | undefined { return this.#accountHolderId; }
  get custodianUserIds(): string[] { return this.#custodianUserIds; }
  /** @deprecated Use custodianUserIds */
  get custodianUserId(): string | undefined { return this.#custodianUserIds[0]; }
  get activatedOn(): Date | undefined { return this.#activatedOn; }
  get bankDetail(): BankDetail | undefined { return this.#bankDetail; }
  get upiDetails(): UPIDetail[] | undefined { return this.#upiDetails; }
  /** @deprecated Use upiDetails; returns primary UPI for backward compatibility */
  get upiDetail(): UPIDetail | undefined { return getPrimaryUpiDetail(this.#upiDetails); }
  get transactions(): ReadonlyArray<Transaction> { return this.#transactions; }

  hasSufficientFunds(amount: number): boolean {
    return this.balance >= amount;
  }

  isActive(): boolean {
    return this.#status === AccountStatus.ACTIVE;
  }
}
