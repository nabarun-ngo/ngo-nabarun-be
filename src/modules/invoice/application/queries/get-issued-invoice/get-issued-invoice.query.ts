import { InvoiceEntityType } from '../../../domain/enums/invoice-entity-type.enum';

export class GetIssuedInvoiceQuery {
  constructor(
    public readonly entityType: InvoiceEntityType,
    public readonly entityId: string,
  ) {}
}
