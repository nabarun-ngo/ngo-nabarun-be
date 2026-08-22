import { DonationPaidCorrespondenceResolver } from './donation-paid-correspondence.resolver';
import { DonationPaidEvent } from '../../domain/events/donation-paid.event';
import { IDonationRepository } from '../../domain/repositories/donation.repository';
import { IDonorRepository } from '../../domain/repositories/donor.repository';
import { DonationMapper } from '../mappers/donation.mapper';
import { EmailTemplateKey } from '../../../../shared/enums/email-template-key';
import { Donor } from '../../domain/aggregates/donor/donor.aggregate';
import { DonorType } from '../../domain/enums/donor-type.enum';
import { DonorStatus } from '../../domain/enums/donor-status.enum';

describe('DonationPaidCorrespondenceResolver', () => {
  let donationRepo: jest.Mocked<Pick<IDonationRepository, 'findById'>>;
  let donorRepo: jest.Mocked<Pick<IDonorRepository, 'findById'>>;
  let resolver: DonationPaidCorrespondenceResolver;

  beforeEach(() => {
    donationRepo = { findById: jest.fn() };
    donorRepo = { findById: jest.fn() };
    resolver = new DonationPaidCorrespondenceResolver(
      donationRepo as unknown as IDonationRepository,
      donorRepo as unknown as IDonorRepository,
    );
    jest.spyOn(DonationMapper, 'toDto').mockReturnValue({ id: 'd1' } as any);
  });

  afterEach(() => jest.restoreAllMocks());

  it('targets DonationPaidEvent', () => {
    expect(resolver.eventType).toBe(DonationPaidEvent);
  });

  it('returns null when the guest donor has no email', async () => {
    donationRepo.findById.mockResolvedValue({ id: 'd1', donorId: 'donor-1' } as any);
    donorRepo.findById.mockResolvedValue(
      new Donor('donor-1', DonorType.GUEST, DonorStatus.ACTIVE, undefined, undefined, 'Guest'),
    );
    const specs = await resolver.resolve(new DonationPaidEvent('d1', 'donor-1', 0));
    expect(specs).toBeNull();
  });

  it('builds a payment-confirmed spec for a member donor', async () => {
    donationRepo.findById.mockResolvedValue({
      id: 'd1',
      donorId: 'donor-1',
      amount: 240,
      currency: 'INR',
      startDate: new Date('2026-01-01'),
      endDate: new Date('2026-02-01'),
      paidOn: new Date('2026-02-05'),
      confirmedBy: undefined,
    } as any);
    donorRepo.findById.mockResolvedValue(
      new Donor('donor-1', DonorType.MEMBER, DonorStatus.ACTIVE, undefined, undefined, undefined, undefined, undefined, undefined, 'user-1'),
    );

    const specs = await resolver.resolve(new DonationPaidEvent('d1', 'donor-1', 240));

    expect(specs).toHaveLength(1);
    const spec = specs![0];
    expect(spec.recipients).toEqual({ mode: 'users', userIds: ['user-1'] });
    expect(spec.channels.email?.templateKey).toBe(EmailTemplateKey.DonationPaid);
    expect(spec.channels.inApp?.title).toBe('Donation payment confirmed');
  });
});
