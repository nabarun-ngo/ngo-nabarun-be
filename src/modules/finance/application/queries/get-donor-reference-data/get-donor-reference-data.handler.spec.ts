import { GetDonorReferenceDataHandler } from './get-donor-reference-data.handler';

describe('GetDonorReferenceDataHandler', () => {
  it('returns donor statuses from the finance reference-data port', async () => {
    const port = {
      getDonorReferenceData: jest.fn().mockResolvedValue({
        donorStatuses: [
          { key: 'ACTIVE', value: 'Active' },
          { key: 'DELETED', value: 'Deleted' },
        ],
        memberEditableDonorStatuses: [{ key: 'ACTIVE', value: 'Active' }],
      }),
    };
    const handler = new GetDonorReferenceDataHandler(port as any);

    await expect(handler.execute()).resolves.toEqual({
      donorStatuses: [
        { key: 'ACTIVE', value: 'Active' },
        { key: 'DELETED', value: 'Deleted' },
      ],
      memberEditableDonorStatuses: [{ key: 'ACTIVE', value: 'Active' }],
    });
  });
});
