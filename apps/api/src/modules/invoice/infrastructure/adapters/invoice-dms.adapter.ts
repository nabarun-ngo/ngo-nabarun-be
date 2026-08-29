import { Injectable } from '@nestjs/common';
import { DmsFacade } from '@nabarun-ngo/nestjs-shared-dms';
import { EntityType } from '../../../../shared/enums/entity-type.enum';
import {
  IInvoiceDocumentStore,
  InvoiceDocumentUpload,
} from '../../domain/ports/invoice-document-store.port';

const INVOICE_DMS_SERVICE_USER_ID = 'system:invoice';
const INVOICE_DMS_SERVICE_PERMISSIONS = [
  'read:documents',
  'create:documents',
  'delete:documents',
];

@Injectable()
export class InvoiceDmsAdapter implements IInvoiceDocumentStore {
  constructor(private readonly dmsFacade: DmsFacade) {}

  async uploadDocument(params: InvoiceDocumentUpload): Promise<string> {
    const mappings = [
      { entityType: EntityType.Invoice, entityId: params.invoiceId },
      ...(params.relatedEntities ?? []),
    ];
    const result = await this.dmsFacade.upload({
      buffer: params.buffer,
      fileName: params.fileName,
      contentType: 'application/pdf',
      mappings,
      visibility: 'PRIVATE',
      userId: INVOICE_DMS_SERVICE_USER_ID,
      userPermissions: INVOICE_DMS_SERVICE_PERMISSIONS,
    });
    return result.id;
  }

  downloadFile(documentId: string): Promise<{ fileName: string; contentType: string; buffer: Buffer }> {
    return this.dmsFacade.download(
      documentId,
      INVOICE_DMS_SERVICE_USER_ID,
      INVOICE_DMS_SERVICE_PERMISSIONS,
    );
  }
}
