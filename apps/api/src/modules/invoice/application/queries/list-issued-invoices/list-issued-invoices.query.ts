import { InvoiceEntityType } from '../../../domain/enums/invoice-entity-type.enum';

export class ListIssuedInvoicesQuery {
  constructor(
    public readonly entityType: InvoiceEntityType,
    public readonly entityIds: string[],
  ) {}
}
