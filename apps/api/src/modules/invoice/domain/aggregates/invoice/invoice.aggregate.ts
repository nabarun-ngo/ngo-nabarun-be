import { AggregateRoot, BusinessException, generateUniqueNDigitNumber } from '@nabarun-ngo/nestjs-shared-core';
import { InvoiceEntityType } from '../../enums/invoice-entity-type.enum';
import { InvoiceStatus } from '../../enums/invoice-status.enum';

export class Invoice extends AggregateRoot<string> {
  #entityType: InvoiceEntityType;
  #entityId: string;
  #status: InvoiceStatus;
  #amount: number;
  #currency: string;
  #issuedOn: Date;
  #voidedOn: Date | undefined;
  #voidReason: string | undefined;
  #documentId: string | undefined;
  #supersededByInvoiceId: string | undefined;

  constructor(
    id: string,
    entityType: InvoiceEntityType,
    entityId: string,
    status: InvoiceStatus,
    amount: number,
    currency: string,
    issuedOn: Date,
    voidedOn?: Date,
    voidReason?: string,
    documentId?: string,
    supersededByInvoiceId?: string,
    createdAt?: Date,
    updatedAt?: Date,
  ) {
    super(id, createdAt, updatedAt);
    this.#entityType = entityType;
    this.#entityId = entityId;
    this.#status = status;
    this.#amount = amount;
    this.#currency = currency;
    this.#issuedOn = issuedOn;
    this.#voidedOn = voidedOn;
    this.#voidReason = voidReason;
    this.#documentId = documentId;
    this.#supersededByInvoiceId = supersededByInvoiceId;
  }

  static issue(props: {
    entityType: InvoiceEntityType;
    entityId: string;
    amount: number;
    currency: string;
    issuedOn: Date;
  }): Invoice {
    if (!props.entityId) throw new BusinessException('Invoice source entity is required');
    if (props.amount <= 0) throw new BusinessException('Invoice amount must be greater than zero');
    return new Invoice(
      `NREC${generateUniqueNDigitNumber(6)}`,
      props.entityType,
      props.entityId,
      InvoiceStatus.ISSUED,
      props.amount,
      props.currency,
      props.issuedOn,
    );
  }

  attachDocument(documentId: string): void {
    if (this.#status !== InvoiceStatus.ISSUED) {
      throw new BusinessException('Cannot attach a file to a voided invoice');
    }
    this.#documentId = documentId;
    this.touch();
  }

  void(reason: string): void {
    if (this.#status !== InvoiceStatus.ISSUED) {
      throw new BusinessException('Invoice is already voided');
    }
    this.#status = InvoiceStatus.VOIDED;
    this.#voidedOn = new Date();
    this.#voidReason = reason;
    this.#documentId = undefined;
    this.touch();
  }

  markSupersededBy(invoiceId: string): void {
    this.#supersededByInvoiceId = invoiceId;
    this.touch();
  }

  get entityType(): InvoiceEntityType {
    return this.#entityType;
  }
  get entityId(): string {
    return this.#entityId;
  }
  get status(): InvoiceStatus {
    return this.#status;
  }
  get amount(): number {
    return this.#amount;
  }
  get currency(): string {
    return this.#currency;
  }
  get issuedOn(): Date {
    return this.#issuedOn;
  }
  get voidedOn(): Date | undefined {
    return this.#voidedOn;
  }
  get voidReason(): string | undefined {
    return this.#voidReason;
  }
  get documentId(): string | undefined {
    return this.#documentId;
  }
  get supersededByInvoiceId(): string | undefined {
    return this.#supersededByInvoiceId;
  }
}
