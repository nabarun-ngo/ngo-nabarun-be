import { DonationRaisedCorrespondenceResolver } from './donation-raised-correspondence.resolver';
import { DonationRaisedEvent } from '../../domain/events/donation-raised.event';
import { IDonationRepository } from '../../domain/repositories/donation.repository';
import { IDonorRepository } from '../../domain/repositories/donor.repository';
import { DonationMapper } from '../mappers/donation.mapper';
import { EmailTemplateKey } from '../../../../shared/enums/email-template-key';
import { Donor } from '../../domain/aggregates/donor/donor.aggregate';
import { DonorType } from '../../domain/enums/donor-type.enum';
import { DonorStatus } from '../../domain/enums/donor-status.enum';

describe('DonationRaisedCorrespondenceResolver', () => {
  let donationRepo: jest.Mocked<Pick<IDonationRepository, 'findById'>>;
  let donorRepo: jest.Mocked<Pick<IDonorRepository, 'findById'>>;
  let resolver: DonationRaisedCorrespondenceResolver;

  beforeEach(() => {
    donationRepo = { findById: jest.fn() };
    donorRepo = { findById: jest.fn() };
    resolver = new DonationRaisedCorrespondenceResolver(
      donationRepo as unknown as IDonationRepository,
      donorRepo as unknown as IDonorRepository,
    );
    jest.spyOn(DonationMapper, 'toDto').mockReturnValue({ id: 'd1' } as any);
  });

  afterEach(() => jest.restoreAllMocks());

  it('targets DonationRaisedEvent', () => {
    expect(resolver.eventType).toBe(DonationRaisedEvent);
  });

  it('returns null when the guest donor has no email', async () => {
    donationRepo.findById.mockResolvedValue({ id: 'd1', donorId: 'donor-1' } as any);
    donorRepo.findById.mockResolvedValue(
      new Donor('donor-1', DonorType.GUEST, DonorStatus.ACTIVE, undefined, undefined, 'Guest'),
    );
    const specs = await resolver.resolve(new DonationRaisedEvent('d1', 'donor-1', 240, 'REGULAR'));
    expect(specs).toBeNull();
  });

  it('builds an in-app + email + push spec for a member donor', async () => {
    donationRepo.findById.mockResolvedValue({
      id: 'd1',
      donorId: 'donor-1',
      amount: 240,
      currency: 'INR',
      startDate: new Date('2026-01-01'),
      endDate: new Date('2026-02-01'),
    } as any);
    donorRepo.findById.mockResolvedValue(
      new Donor('donor-1', DonorType.MEMBER, DonorStatus.ACTIVE, undefined, undefined, undefined, undefined, undefined, undefined, 'user-1'),
    );

    const specs = await resolver.resolve(new DonationRaisedEvent('d1', 'donor-1', 240, 'REGULAR'));

    expect(specs).toHaveLength(1);
    const spec = specs![0];
    expect(spec.recipients).toEqual({ mode: 'users', userIds: ['user-1'] });
    expect(spec.channels.email?.templateKey).toBe(EmailTemplateKey.DonationCreated);
    expect(spec.channels.inApp?.title).toBe('Donation raised');
  });

  it('builds an email-only spec for a guest donor with email', async () => {
    donationRepo.findById.mockResolvedValue({
      id: 'd1',
      donorId: 'donor-guest',
      amount: 100,
      currency: 'INR',
    } as any);
    donorRepo.findById.mockResolvedValue(
      new Donor('donor-guest', DonorType.GUEST, DonorStatus.ACTIVE, undefined, undefined, 'Guest', 'guest@example.com'),
    );

    const specs = await resolver.resolve(new DonationRaisedEvent('d1', 'donor-guest', 100, 'REGULAR'));

    const spec = specs![0];
    expect(spec.recipients).toEqual({ mode: 'users', userIds: [] });
    expect(spec.channels.inApp).toBeUndefined();
    expect(spec.channels.email?.overrideEmails).toEqual(['guest@example.com']);
  });
});
