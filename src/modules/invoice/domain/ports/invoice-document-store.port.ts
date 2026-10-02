export const IInvoiceDocumentStore = Symbol('IInvoiceDocumentStore');

export type InvoiceDocumentUpload = {
  buffer: Buffer;
  fileName: string;
  invoiceId: string;
  relatedEntities?: { entityType: string; entityId: string }[];
};

export interface IInvoiceDocumentStore {
  uploadDocument(params: InvoiceDocumentUpload): Promise<string>;
  downloadFile(documentId: string): Promise<{ fileName: string; contentType: string; buffer: Buffer }>;
}
