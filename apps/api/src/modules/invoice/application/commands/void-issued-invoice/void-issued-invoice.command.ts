import { InvoiceEntityType } from '../../../domain/enums/invoice-entity-type.enum';

export class VoidIssuedInvoiceCommand {
  constructor(
    public readonly params: {
      entityType: InvoiceEntityType;
      entityId: string;
      reason: string;
    },
  ) {}
}
