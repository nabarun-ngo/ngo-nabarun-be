import { DonationRaisedCorrespondenceResolver } from './donation-raised-correspondence.resolver';
import { DonationRaisedEvent } from '../../domain/events/donation-raised.event';
import { IDonationRepository } from '../../domain/repositories/donation.repository';
import { DonationMapper } from '../mappers/donation.mapper';
import { EmailTemplateKey } from '../../../../shared/email-template-key';

describe('DonationRaisedCorrespondenceResolver', () => {
  let repo: jest.Mocked<Pick<IDonationRepository, 'findById'>>;
  let resolver: DonationRaisedCorrespondenceResolver;

  beforeEach(() => {
    repo = { findById: jest.fn() };
    resolver = new DonationRaisedCorrespondenceResolver(repo as unknown as IDonationRepository);
    jest.spyOn(DonationMapper, 'toDto').mockReturnValue({ id: 'd1' } as any);
  });

  afterEach(() => jest.restoreAllMocks());

  it('targets DonationRaisedEvent', () => {
    expect(resolver.eventType).toBe(DonationRaisedEvent);
  });

  it('returns null when the donation has no donor email', async () => {
    repo.findById.mockResolvedValue({ id: 'd1', donorEmail: undefined } as any);
    const specs = await resolver.resolve(new DonationRaisedEvent('d1', undefined, undefined, 240, 'REGULAR'));
    expect(specs).toBeNull();
  });

  it('builds an in-app + email + push spec for a registered donor', async () => {
    repo.findById.mockResolvedValue({
      id: 'd1',
      donorId: 'donor-1',
      donorEmail: 'donor@example.com',
      amount: 240,
      currency: 'INR',
      startDate: new Date('2026-01-01'),
      endDate: new Date('2026-02-01'),
    } as any);

    const specs = await resolver.resolve(new DonationRaisedEvent('d1', 'donor-1', 'donor@example.com', 240, 'REGULAR'));

    expect(specs).toHaveLength(1);
    const spec = specs![0];
    expect(spec.recipients).toEqual({ mode: 'users', userIds: ['donor-1'] });
    expect(spec.channels.email?.templateKey).toBe(EmailTemplateKey.DonationCreated);
    expect(spec.channels.inApp?.title).toBe('Donation raised');
  });

  it('builds an email-only spec for a guest donor (no donorId)', async () => {
    repo.findById.mockResolvedValue({
      id: 'd1',
      donorId: undefined,
      donorEmail: 'guest@example.com',
      amount: 100,
      currency: 'INR',
    } as any);

    const specs = await resolver.resolve(new DonationRaisedEvent('d1', undefined, 'guest@example.com', 100, 'REGULAR'));

    const spec = specs![0];
    expect(spec.recipients).toEqual({ mode: 'users', userIds: [] });
    expect(spec.channels.inApp).toBeUndefined();
    expect(spec.channels.email?.overrideEmails).toEqual(['guest@example.com']);
  });
});
