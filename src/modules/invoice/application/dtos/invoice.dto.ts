import { InvoiceStatus } from '../../domain/enums/invoice-status.enum';

export class InvoiceSummaryDto {
  id!: string;
  entityId!: string;
  status!: InvoiceStatus;
  documentId?: string;
  issuedOn!: Date;
}
