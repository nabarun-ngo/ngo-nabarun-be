import type { IUserLookupPort } from '@nabarun-ngo/nestjs-shared-core';
import type { IDonationRepository } from '../../../domain/repositories/donation.repository';
import type { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { DonorType } from '../../../domain/enums/donor-type.enum';
import { ListDonationsHandler } from './list-donations.handler';
import { ListDonationsQuery } from './list-donations.query';

describe('ListDonationsHandler', () => {
  const donationRepository = {
    findPaged: jest.fn(),
  };
  const donorRepository = {
    findByUserProfileId: jest.fn(),
    findById: jest.fn(),
  };
  const userLookup = {
    findByIds: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    donationRepository.findPaged.mockResolvedValue({
      content: [],
      totalSize: 0,
      pageIndex: 0,
      pageSize: 20,
    });
  });

  it.each([
    ['Y' as const, DonorType.GUEST],
    ['N' as const, DonorType.MEMBER],
  ])('maps isGuest=%s to the repository donor type', async (isGuest, donorType) => {
    const handler = new ListDonationsHandler(
      donationRepository as unknown as IDonationRepository,
      donorRepository as unknown as IDonorRepository,
      userLookup as unknown as IUserLookupPort,
    );

    await handler.execute(new ListDonationsQuery({ isGuest }));

    expect(donationRepository.findPaged).toHaveBeenCalledWith(
      expect.objectContaining({
        props: expect.objectContaining({ donorType }),
      }),
    );
  });

  it('passes the project activity filter to the repository', async () => {
    const handler = new ListDonationsHandler(
      donationRepository as unknown as IDonationRepository,
      donorRepository as unknown as IDonorRepository,
      userLookup as unknown as IUserLookupPort,
    );

    await handler.execute(new ListDonationsQuery({ forEventId: 'activity-1' }));

    expect(donationRepository.findPaged).toHaveBeenCalledWith(
      expect.objectContaining({
        props: expect.objectContaining({ forEventId: 'activity-1' }),
      }),
    );
  });
});
