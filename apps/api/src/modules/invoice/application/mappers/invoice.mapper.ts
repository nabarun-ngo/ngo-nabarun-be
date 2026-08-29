import { Invoice } from '../../domain/aggregates/invoice/invoice.aggregate';
import { InvoiceSummaryDto } from '../dtos/invoice.dto';

export class InvoiceMapper {
  static toSummary(invoice: Invoice): InvoiceSummaryDto {
    return {
      id: invoice.id,
      entityId: invoice.entityId,
      status: invoice.status,
      documentId: invoice.documentId,
      issuedOn: invoice.issuedOn,
    };
  }
}
