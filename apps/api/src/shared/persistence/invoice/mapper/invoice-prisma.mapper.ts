import { MapperUtils } from '../../finance/mapper/mapper-utils';
import { Invoice } from '../../../../modules/invoice/domain/aggregates/invoice/invoice.aggregate';
import { InvoiceEntityType } from '../../../../modules/invoice/domain/enums/invoice-entity-type.enum';
import { InvoiceStatus } from '../../../../modules/invoice/domain/enums/invoice-status.enum';
import { Prisma } from '../../prisma/client';
import type { Invoice as InvoiceRow } from '../../prisma/client';

export class InvoicePrismaMapper {
  static toInvoiceDomain(row: InvoiceRow | null): Invoice | null {
    if (!row) return null;
    return new Invoice(
      row.id,
      row.entityType as InvoiceEntityType,
      row.entityId,
      row.status as InvoiceStatus,
      Number(row.amount),
      row.currency,
      row.issuedOn,
      MapperUtils.nullToUndefined(row.voidedOn),
      MapperUtils.nullToUndefined(row.voidReason),
      MapperUtils.nullToUndefined(row.documentId),
      MapperUtils.nullToUndefined(row.supersededByInvoiceId),
      row.createdAt,
      row.updatedAt,
    );
  }

  static toInvoiceCreatePersistence(invoice: Invoice): Prisma.InvoiceUncheckedCreateInput {
    return {
      id: invoice.id,
      entityType: invoice.entityType,
      entityId: invoice.entityId,
      status: invoice.status,
      amount: invoice.amount,
      currency: invoice.currency,
      issuedOn: invoice.issuedOn,
      voidedOn: MapperUtils.undefinedToNull(invoice.voidedOn),
      voidReason: MapperUtils.undefinedToNull(invoice.voidReason),
      documentId: MapperUtils.undefinedToNull(invoice.documentId),
      supersededByInvoiceId: MapperUtils.undefinedToNull(invoice.supersededByInvoiceId),
      createdAt: invoice.createdAt,
      updatedAt: invoice.updatedAt,
    };
  }

  static toInvoiceUpdatePersistence(invoice: Invoice): Prisma.InvoiceUncheckedUpdateInput {
    return {
      status: invoice.status,
      amount: invoice.amount,
      currency: invoice.currency,
      issuedOn: invoice.issuedOn,
      voidedOn: MapperUtils.undefinedToNull(invoice.voidedOn),
      voidReason: MapperUtils.undefinedToNull(invoice.voidReason),
      documentId: MapperUtils.undefinedToNull(invoice.documentId),
      supersededByInvoiceId: MapperUtils.undefinedToNull(invoice.supersededByInvoiceId),
      updatedAt: invoice.updatedAt,
    };
  }
}
