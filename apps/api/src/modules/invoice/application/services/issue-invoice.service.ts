import { Inject, Injectable, Logger } from '@nestjs/common';
import { Invoice } from '../../domain/aggregates/invoice/invoice.aggregate';
import { InvoiceEntityType } from '../../domain/enums/invoice-entity-type.enum';
import { InvoiceStatus } from '../../domain/enums/invoice-status.enum';
import { IInvoiceRepository } from '../../domain/repositories/invoice.repository';
import { IInvoiceDocumentStore } from '../../domain/ports/invoice-document-store.port';
import { InvoiceSummaryDto } from '../dtos/invoice.dto';
import { InvoiceMapper } from '../mappers/invoice.mapper';

export type InvoiceDocumentPayload = {
  buffer: Buffer;
  fileName: string;
  relatedEntities?: { entityType: string; entityId: string }[];
};

export type IssueInvoiceParams = {
  entityType: InvoiceEntityType;
  entityId: string;
  amount: number;
  currency: string;
  issuedOn: Date;
  documentFactory?: (invoice: InvoiceSummaryDto) => Promise<InvoiceDocumentPayload>;
};

@Injectable()
export class IssueInvoiceService {
  private readonly logger = new Logger(IssueInvoiceService.name);

  constructor(
    @Inject(IInvoiceRepository) private readonly invoiceRepository: IInvoiceRepository,
    @Inject(IInvoiceDocumentStore) private readonly documentStore: IInvoiceDocumentStore,
  ) {}

  async issue(params: IssueInvoiceParams): Promise<Invoice> {
    const existing = await this.invoiceRepository.findIssuedByEntity(params.entityType, params.entityId);
    if (existing) return existing;

    const previous = await this.invoiceRepository.findAll({
      entityType: params.entityType,
      entityId: params.entityId,
      status: InvoiceStatus.VOIDED,
    });

    const invoice = Invoice.issue({
      entityType: params.entityType,
      entityId: params.entityId,
      amount: params.amount,
      currency: params.currency,
      issuedOn: params.issuedOn,
    });
    await this.invoiceRepository.create(invoice);

    if (params.documentFactory) {
      const document = await params.documentFactory(InvoiceMapper.toSummary(invoice));
      const documentId = await this.documentStore.uploadDocument({
        buffer: document.buffer,
        fileName: document.fileName,
        invoiceId: invoice.id,
        relatedEntities: document.relatedEntities,
      });
      invoice.attachDocument(documentId);
      await this.invoiceRepository.update(invoice.id, invoice);
    }

    const saved = invoice;
    const latestVoided = previous[0];
    if (latestVoided) {
      latestVoided.markSupersededBy(saved.id);
      await this.invoiceRepository.update(latestVoided.id, latestVoided);
    }
    this.logger.log(`Issued invoice ${saved.id} for ${params.entityType} ${params.entityId}`);
    return saved;
  }

  async voidIssued(entityType: InvoiceEntityType, entityId: string, reason: string): Promise<void> {
    const issued = await this.invoiceRepository.findIssuedByEntity(entityType, entityId);
    if (!issued) return;
    issued.void(reason);
    await this.invoiceRepository.update(issued.id, issued);
  }
}
