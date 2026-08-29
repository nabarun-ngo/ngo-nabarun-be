import { Inject, Injectable } from '@nestjs/common';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { InvoiceEntityType } from '../../domain/enums/invoice-entity-type.enum';
import { IInvoiceDocumentStore } from '../../domain/ports/invoice-document-store.port';
import { IssueInvoiceCommand } from '../commands/issue-invoice/issue-invoice.command';
import { VoidIssuedInvoiceCommand } from '../commands/void-issued-invoice/void-issued-invoice.command';
import { InvoiceSummaryDto } from '../dtos/invoice.dto';
import { GetIssuedInvoiceQuery } from '../queries/get-issued-invoice/get-issued-invoice.query';
import { ListIssuedInvoicesQuery } from '../queries/list-issued-invoices/list-issued-invoices.query';
import { IssueInvoiceParams } from './issue-invoice.service';

@Injectable()
export class InvoiceFacade {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
    @Inject(IInvoiceDocumentStore) private readonly documentStore: IInvoiceDocumentStore,
  ) {}

  issue(params: IssueInvoiceParams): Promise<InvoiceSummaryDto> {
    return this.commandBus.execute(new IssueInvoiceCommand(params));
  }

  voidIssued(entityType: InvoiceEntityType, entityId: string, reason: string): Promise<void> {
    return this.commandBus.execute(new VoidIssuedInvoiceCommand({ entityType, entityId, reason }));
  }

  findIssuedByEntity(
    entityType: InvoiceEntityType,
    entityId: string,
  ): Promise<InvoiceSummaryDto | null> {
    return this.queryBus.execute(new GetIssuedInvoiceQuery(entityType, entityId));
  }

  findIssuedByEntities(
    entityType: InvoiceEntityType,
    entityIds: string[],
  ): Promise<InvoiceSummaryDto[]> {
    return this.queryBus.execute(new ListIssuedInvoicesQuery(entityType, entityIds));
  }

  downloadDocument(
    documentId: string,
  ): Promise<{ fileName: string; contentType: string; buffer: Buffer }> {
    return this.documentStore.downloadFile(documentId);
  }
}
