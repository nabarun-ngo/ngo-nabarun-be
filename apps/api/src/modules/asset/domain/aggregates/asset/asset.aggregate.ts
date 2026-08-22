import { randomUUID } from 'crypto';
import { AggregateRoot, BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { AssetCategory, AssetStatus } from '../../enums/asset.enum';

export interface AssetCustodyRecordProps {
  id: string;
  custodianUserId: string;
  assignedAt: Date;
  assignedById?: string;
  returnedAt?: Date;
  returnedById?: string;
  notes?: string;
}

export interface AssetFilter {
  status?: string;
  category?: string;
  custodianUserId?: string;
  projectId?: string;
}

export interface AssetCreateProps {
  name: string;
  category: AssetCategory;
  serialNumber?: string;
  location?: string;
  status?: AssetStatus;
  projectId?: string;
  expenseId?: string;
  purchaseDate?: Date;
  purchaseCost?: number;
  currency?: string;
  currentValue?: number;
  depreciationMethodNotes?: string;
  maintenanceNotes?: string;
  createdById?: string;
}

export interface AssetUpdateProps {
  name?: string;
  category?: AssetCategory;
  serialNumber?: string | null;
  location?: string | null;
  status?: AssetStatus;
  projectId?: string | null;
  expenseId?: string | null;
  purchaseDate?: Date | null;
  purchaseCost?: number | null;
  currency?: string | null;
  currentValue?: number | null;
  depreciationMethodNotes?: string | null;
  maintenanceNotes?: string | null;
  updatedById?: string;
}

export class Asset extends AggregateRoot<string> {
  #name: string;
  #category: AssetCategory;
  #serialNumber?: string;
  #location?: string;
  #status: AssetStatus;
  #custodianUserId?: string;
  #projectId?: string;
  #expenseId?: string;
  #purchaseDate?: Date;
  #purchaseCost?: number;
  #currency?: string;
  #currentValue?: number;
  #depreciationMethodNotes?: string;
  #maintenanceNotes?: string;
  #createdById?: string;
  #updatedById?: string;
  #custodyHistory: AssetCustodyRecordProps[];

  constructor(
    id: string,
    name: string,
    category: AssetCategory,
    status: AssetStatus,
    serialNumber: string | undefined,
    location: string | undefined,
    custodianUserId: string | undefined,
    projectId: string | undefined,
    expenseId: string | undefined,
    purchaseDate: Date | undefined,
    purchaseCost: number | undefined,
    currency: string | undefined,
    currentValue: number | undefined,
    depreciationMethodNotes: string | undefined,
    maintenanceNotes: string | undefined,
    createdById: string | undefined,
    updatedById: string | undefined,
    custodyHistory: AssetCustodyRecordProps[] | undefined,
    createdAt?: Date,
    updatedAt?: Date,
  ) {
    super(id, createdAt, updatedAt);
    this.#name = name;
    this.#category = category;
    this.#status = status;
    this.#serialNumber = serialNumber;
    this.#location = location;
    this.#custodianUserId = custodianUserId;
    this.#projectId = projectId;
    this.#expenseId = expenseId;
    this.#purchaseDate = purchaseDate;
    this.#purchaseCost = purchaseCost;
    this.#currency = currency;
    this.#currentValue = currentValue;
    this.#depreciationMethodNotes = depreciationMethodNotes;
    this.#maintenanceNotes = maintenanceNotes;
    this.#createdById = createdById;
    this.#updatedById = updatedById;
    this.#custodyHistory = custodyHistory ?? [];
  }

  static create(props: AssetCreateProps): Asset {
    if (!props.name?.trim()) {
      throw new BusinessException('Asset name is required');
    }
    const status = props.status ?? AssetStatus.AVAILABLE;
    if (status === AssetStatus.ASSIGNED) {
      throw new BusinessException('New assets cannot start as Assigned; use assign custody after create');
    }
    Asset.assertMoneyFields(props.purchaseCost, props.currentValue, props.currency);

    return new Asset(
      randomUUID(),
      props.name.trim(),
      props.category,
      status,
      props.serialNumber,
      props.location,
      undefined,
      props.projectId,
      props.expenseId,
      props.purchaseDate,
      props.purchaseCost,
      props.currency,
      props.currentValue,
      props.depreciationMethodNotes,
      props.maintenanceNotes,
      props.createdById,
      undefined,
      [],
    );
  }

  update(props: AssetUpdateProps): void {
    if (props.name !== undefined) {
      if (!props.name.trim()) {
        throw new BusinessException('Asset name is required');
      }
      this.#name = props.name.trim();
    }
    if (props.category !== undefined) this.#category = props.category;
    if (props.serialNumber !== undefined) this.#serialNumber = props.serialNumber ?? undefined;
    if (props.location !== undefined) this.#location = props.location ?? undefined;
    if (props.projectId !== undefined) this.#projectId = props.projectId ?? undefined;
    if (props.expenseId !== undefined) this.#expenseId = props.expenseId ?? undefined;
    if (props.purchaseDate !== undefined) this.#purchaseDate = props.purchaseDate ?? undefined;
    if (props.purchaseCost !== undefined) this.#purchaseCost = props.purchaseCost ?? undefined;
    if (props.currency !== undefined) this.#currency = props.currency ?? undefined;
    if (props.currentValue !== undefined) this.#currentValue = props.currentValue ?? undefined;
    if (props.depreciationMethodNotes !== undefined) {
      this.#depreciationMethodNotes = props.depreciationMethodNotes ?? undefined;
    }
    if (props.maintenanceNotes !== undefined) {
      this.#maintenanceNotes = props.maintenanceNotes ?? undefined;
    }
    if (props.status !== undefined) {
      if (props.status === AssetStatus.ASSIGNED && !this.#custodianUserId) {
        throw new BusinessException('Cannot set status to Assigned without a custodian; use assign custody');
      }
      this.#status = props.status;
    }
    if (props.updatedById !== undefined) this.#updatedById = props.updatedById;

    Asset.assertMoneyFields(this.#purchaseCost, this.#currentValue, this.#currency);
    this.touch();
  }

  assignCustody(custodianUserId: string, assignedById?: string, notes?: string): void {
    if (!custodianUserId?.trim()) {
      throw new BusinessException('Custodian is required to assign an asset');
    }
    if (this.#status === AssetStatus.RETIRED) {
      throw new BusinessException('Retired assets cannot be assigned');
    }

    const now = new Date();
    for (const record of this.#custodyHistory) {
      if (!record.returnedAt) {
        record.returnedAt = now;
        record.returnedById = assignedById;
      }
    }

    this.#custodyHistory.push({
      id: randomUUID(),
      custodianUserId: custodianUserId.trim(),
      assignedAt: now,
      assignedById,
      notes,
    });
    this.#custodianUserId = custodianUserId.trim();
    this.#status = AssetStatus.ASSIGNED;
    this.#updatedById = assignedById;
    this.touch();
  }

  returnCustody(returnedById?: string, notes?: string): void {
    const open = this.#custodyHistory.find((r) => !r.returnedAt);
    if (!open) {
      throw new BusinessException('Asset has no open custody to return');
    }

    open.returnedAt = new Date();
    open.returnedById = returnedById;
    if (notes !== undefined) {
      open.notes = open.notes ? `${open.notes}\n${notes}` : notes;
    }

    this.#custodianUserId = undefined;
    if (this.#status !== AssetStatus.RETIRED) {
      this.#status = AssetStatus.AVAILABLE;
    }
    this.#updatedById = returnedById;
    this.touch();
  }

  private static assertMoneyFields(
    purchaseCost: number | undefined,
    currentValue: number | undefined,
    currency: string | undefined,
  ): void {
    if (purchaseCost !== undefined && purchaseCost < 0) {
      throw new BusinessException('Purchase cost must be zero or greater');
    }
    if (currentValue !== undefined && currentValue < 0) {
      throw new BusinessException('Current value must be zero or greater');
    }
    const hasAmount = purchaseCost !== undefined || currentValue !== undefined;
    if (hasAmount && !currency?.trim()) {
      throw new BusinessException('Currency is required when purchase cost or current value is set');
    }
  }

  get name(): string { return this.#name; }
  get category(): AssetCategory { return this.#category; }
  get serialNumber(): string | undefined { return this.#serialNumber; }
  get location(): string | undefined { return this.#location; }
  get status(): AssetStatus { return this.#status; }
  get custodianUserId(): string | undefined { return this.#custodianUserId; }
  get projectId(): string | undefined { return this.#projectId; }
  get expenseId(): string | undefined { return this.#expenseId; }
  get purchaseDate(): Date | undefined { return this.#purchaseDate; }
  get purchaseCost(): number | undefined { return this.#purchaseCost; }
  get currency(): string | undefined { return this.#currency; }
  get currentValue(): number | undefined { return this.#currentValue; }
  get depreciationMethodNotes(): string | undefined { return this.#depreciationMethodNotes; }
  get maintenanceNotes(): string | undefined { return this.#maintenanceNotes; }
  get createdById(): string | undefined { return this.#createdById; }
  get updatedById(): string | undefined { return this.#updatedById; }
  get custodyHistory(): AssetCustodyRecordProps[] {
    return this.#custodyHistory.map((r) => ({ ...r }));
  }
}
