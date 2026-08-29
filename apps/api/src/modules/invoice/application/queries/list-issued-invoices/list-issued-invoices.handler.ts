import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { InvoiceStatus } from '../../../domain/enums/invoice-status.enum';
import { IInvoiceRepository } from '../../../domain/repositories/invoice.repository';
import { InvoiceSummaryDto } from '../../dtos/invoice.dto';
import { InvoiceMapper } from '../../mappers/invoice.mapper';
import { ListIssuedInvoicesQuery } from './list-issued-invoices.query';

@QueryHandler(ListIssuedInvoicesQuery)
@Injectable()
export class ListIssuedInvoicesHandler
  implements IQueryHandler<ListIssuedInvoicesQuery, InvoiceSummaryDto[]>
{
  constructor(@Inject(IInvoiceRepository) private readonly invoiceRepository: IInvoiceRepository) {}

  async execute(query: ListIssuedInvoicesQuery): Promise<InvoiceSummaryDto[]> {
    if (!query.entityIds.length) return [];
    const invoices = await this.invoiceRepository.findAll({
      entityType: query.entityType,
      entityIds: query.entityIds,
      status: InvoiceStatus.ISSUED,
    });
    return invoices.map((invoice) => InvoiceMapper.toSummary(invoice));
  }
}
