import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { Prisma, PrismaClient } from '../../prisma/client';
import type {
  InvoiceWhereInput,
  InvoiceWhereUniqueInput,
  InvoiceUncheckedCreateInput,
  InvoiceUncheckedUpdateInput,
  InvoiceOrderByWithRelationInput,
} from '../../prisma/models/Invoice';
import { IInvoiceRepository, InvoiceFilter } from '../../../../modules/invoice/domain/repositories/invoice.repository';
import { Invoice } from '../../../../modules/invoice/domain/aggregates/invoice/invoice.aggregate';
import { InvoiceEntityType } from '../../../../modules/invoice/domain/enums/invoice-entity-type.enum';
import { InvoiceStatus } from '../../../../modules/invoice/domain/enums/invoice-status.enum';
import { InvoicePrismaMapper } from '../mapper/invoice-prisma.mapper';

export type InvoicePersistence = Prisma.InvoiceGetPayload<object>;

@Injectable()
export class InvoicePrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'invoice',
    Invoice,
    string,
    InvoiceFilter,
    InvoicePersistence,
    InvoiceWhereInput,
    InvoiceWhereUniqueInput,
    InvoiceUncheckedCreateInput,
    InvoiceUncheckedUpdateInput,
    InvoiceOrderByWithRelationInput
  >
  implements IInvoiceRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'invoice');
  }

  protected toDomain(row: InvoicePersistence): Invoice {
    return InvoicePrismaMapper.toInvoiceDomain(row)!;
  }

  protected toCreateInput(invoice: Invoice): InvoiceUncheckedCreateInput {
    return InvoicePrismaMapper.toInvoiceCreatePersistence(invoice);
  }

  protected toUpdateInput(_id: string, invoice: Invoice): InvoiceUncheckedUpdateInput {
    return InvoicePrismaMapper.toInvoiceUpdatePersistence(invoice);
  }

  protected toUniqueWhere(id: string): InvoiceWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(props?: InvoiceFilter): InvoiceWhereInput {
    return {
      ...(props?.entityType ? { entityType: props.entityType } : {}),
      ...(props?.entityId ? { entityId: props.entityId } : {}),
      ...(props?.entityIds && props.entityIds.length > 0 ? { entityId: { in: props.entityIds } } : {}),
      ...(props?.status ? { status: props.status } : {}),
      deletedAt: null,
    };
  }

  async findIssuedByEntity(entityType: InvoiceEntityType, entityId: string): Promise<Invoice | null> {
    const items = await this.findAll({ entityType, entityId, status: InvoiceStatus.ISSUED });
    return items[0] ?? null;
  }
}
