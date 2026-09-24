import { FinanceReferenceDataAdapter } from './finance-reference-data.adapter';

describe('FinanceReferenceDataAdapter', () => {
  it('loads donor reference data from the finance namespace', async () => {
    const jsonStore = {
      get: jest
        .fn()
        .mockResolvedValueOnce({ items: [{ key: 'ACTIVE', value: 'Active' }] })
        .mockResolvedValueOnce({ items: [{ key: 'PAUSED', value: 'Paused' }] })
        .mockResolvedValueOnce({ statusesRequiringEndDate: ['PAUSED', 'WAIVED'] }),
    };
    const adapter = new FinanceReferenceDataAdapter(jsonStore as any);

    await expect(adapter.getDonorReferenceData()).resolves.toEqual({
      donorStatuses: [{ key: 'ACTIVE', value: 'Active' }],
      memberEditableDonorStatuses: [{ key: 'PAUSED', value: 'Paused' }],
      statusesRequiringEndDate: ['PAUSED', 'WAIVED'],
    });
    expect(jsonStore.get).toHaveBeenNthCalledWith(1, 'donor-statuses', 'finance-reference-data');
    expect(jsonStore.get).toHaveBeenNthCalledWith(
      2,
      'member-editable-donor-statuses',
      'finance-reference-data',
    );
    expect(jsonStore.get).toHaveBeenNthCalledWith(3, 'donor-status-rules', 'finance-reference-data');
  });
});
