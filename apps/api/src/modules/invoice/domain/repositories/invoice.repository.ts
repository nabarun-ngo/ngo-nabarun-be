import { IRepository } from '@nabarun-ngo/nestjs-shared-core';
import { Invoice } from '../aggregates/invoice/invoice.aggregate';
import { InvoiceEntityType } from '../enums/invoice-entity-type.enum';
import { InvoiceStatus } from '../enums/invoice-status.enum';

export interface InvoiceFilter {
  entityType?: InvoiceEntityType;
  entityId?: string;
  entityIds?: string[];
  status?: InvoiceStatus;
}

export const IInvoiceRepository = Symbol('IInvoiceRepository');

export interface IInvoiceRepository extends IRepository<Invoice, string, InvoiceFilter> {
  findIssuedByEntity(entityType: InvoiceEntityType, entityId: string): Promise<Invoice | null>;
}
