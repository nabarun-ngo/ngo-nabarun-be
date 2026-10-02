import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IInvoiceRepository } from '../../../domain/repositories/invoice.repository';
import { InvoiceSummaryDto } from '../../dtos/invoice.dto';
import { InvoiceMapper } from '../../mappers/invoice.mapper';
import { GetIssuedInvoiceQuery } from './get-issued-invoice.query';

@QueryHandler(GetIssuedInvoiceQuery)
@Injectable()
export class GetIssuedInvoiceHandler
  implements IQueryHandler<GetIssuedInvoiceQuery, InvoiceSummaryDto | null>
{
  constructor(@Inject(IInvoiceRepository) private readonly invoiceRepository: IInvoiceRepository) {}

  async execute(query: GetIssuedInvoiceQuery): Promise<InvoiceSummaryDto | null> {
    const invoice = await this.invoiceRepository.findIssuedByEntity(query.entityType, query.entityId);
    return invoice ? InvoiceMapper.toSummary(invoice) : null;
  }
}
