import { EntityType } from '../../../../shared/enums/entity-type.enum';
import { InvoiceDmsAdapter } from './invoice-dms.adapter';

describe('InvoiceDmsAdapter', () => {
  it('uploads the file mapped to the invoice and related entities', async () => {
    const dmsFacade = {
      upload: jest.fn().mockResolvedValue({ id: 'doc-1' }),
    };
    const adapter = new InvoiceDmsAdapter(dmsFacade as any);

    await adapter.uploadDocument({
      buffer: Buffer.from('pdf'),
      fileName: 'Receipt-NDON111111.pdf',
      invoiceId: 'NREC000001',
      relatedEntities: [
        { entityType: EntityType.Donation, entityId: 'NDON111111' },
        { entityType: EntityType.Donor, entityId: 'NDNR222222' },
      ],
    });

    expect(dmsFacade.upload).toHaveBeenCalledWith(
      expect.objectContaining({
        mappings: [
          { entityType: EntityType.Invoice, entityId: 'NREC000001' },
          { entityType: EntityType.Donation, entityId: 'NDON111111' },
          { entityType: EntityType.Donor, entityId: 'NDNR222222' },
        ],
      }),
    );
  });
});
