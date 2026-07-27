import { DonationPaidCorrespondenceResolver } from './donation-paid-correspondence.resolver';
import { DonationPaidEvent } from '../../domain/events/donation-paid.event';
import { IDonationRepository } from '../../domain/repositories/donation.repository';
import { DonationMapper } from '../mappers/donation.mapper';
import { EmailTemplateKey } from '../../../../shared/email-template-key';

describe('DonationPaidCorrespondenceResolver', () => {
  let repo: jest.Mocked<Pick<IDonationRepository, 'findById'>>;
  let resolver: DonationPaidCorrespondenceResolver;

  beforeEach(() => {
    repo = { findById: jest.fn() };
    resolver = new DonationPaidCorrespondenceResolver(repo as unknown as IDonationRepository);
    jest.spyOn(DonationMapper, 'toDto').mockReturnValue({ id: 'd1' } as any);
  });

  afterEach(() => jest.restoreAllMocks());

  it('targets DonationPaidEvent', () => {
    expect(resolver.eventType).toBe(DonationPaidEvent);
  });

  it('returns null when the donation has no donor email', async () => {
    repo.findById.mockResolvedValue({ id: 'd1', donorEmail: undefined } as any);
    const specs = await resolver.resolve(new DonationPaidEvent('d1', undefined, undefined, 0));
    expect(specs).toBeNull();
  });

  it('builds a payment-confirmed spec for a registered donor', async () => {
    repo.findById.mockResolvedValue({
      id: 'd1',
      donorId: 'donor-1',
      donorEmail: 'donor@example.com',
      amount: 240,
      currency: 'INR',
      startDate: new Date('2026-01-01'),
      endDate: new Date('2026-02-01'),
      paidOn: new Date('2026-02-05'),
      confirmedBy: undefined,
    } as any);

    const specs = await resolver.resolve(new DonationPaidEvent('d1', 'donor-1', 'donor@example.com', 240));

    expect(specs).toHaveLength(1);
    const spec = specs![0];
    expect(spec.recipients).toEqual({ mode: 'users', userIds: ['donor-1'] });
    expect(spec.channels.email?.templateKey).toBe(EmailTemplateKey.DonationPaid);
    expect(spec.channels.inApp?.title).toBe('Donation payment confirmed');
  });
});
