import { InvoiceEntityType } from '../../../domain/enums/invoice-entity-type.enum';
import { InvoiceSummaryDto } from '../../dtos/invoice.dto';
import { InvoiceDocumentPayload } from '../../services/issue-invoice.service';

export class IssueInvoiceCommand {
  constructor(
    public readonly params: {
      entityType: InvoiceEntityType;
      entityId: string;
      amount: number;
      currency: string;
      issuedOn: Date;
      documentFactory?: (invoice: InvoiceSummaryDto) => Promise<InvoiceDocumentPayload>;
    },
  ) {}
}
