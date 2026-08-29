import { FinanceDmsAdapter } from './finance-dms.adapter';

describe('FinanceDmsAdapter', () => {
  it('lists documents for an entity using the finance service actor', async () => {
    const dmsFacade = {
      listByEntity: jest.fn().mockResolvedValue([{ id: 'doc-1' }]),
    };
    const adapter = new FinanceDmsAdapter(dmsFacade as any);
    await expect(adapter.getDocuments('donation', 'NDON111111')).resolves.toEqual([{ id: 'doc-1' }]);
    expect(dmsFacade.listByEntity).toHaveBeenCalledWith(
      'donation',
      'NDON111111',
      'system:finance',
      ['read:documents', 'create:documents', 'delete:documents'],
    );
  });
});
