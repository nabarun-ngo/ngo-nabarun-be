import { EventBus } from '@nestjs/cqrs';
import { CreateDonationHandler } from './create-donation.handler';
import { CreateDonationCommand } from './create-donation.command';
import { DonationType } from '../../../domain/enums/donation-type.enum';
import { Donation } from '../../../domain/aggregates/donation/donation.aggregate';
import { Donor } from '../../../domain/aggregates/donor/donor.aggregate';
import { DonorType } from '../../../domain/enums/donor-type.enum';
import { DonorStatus } from '../../../domain/enums/donor-status.enum';

describe('CreateDonationHandler', () => {
  const donor = new Donor('donor-1', DonorType.GUEST, DonorStatus.ACTIVE, undefined, undefined, 'Guest User');

  it('creates and persists a one-time donation for a donor', async () => {
    const created = Donation.create({
      type: DonationType.ONETIME,
      amount: 500,
      donorId: 'donor-1',
    });

    const donationRepository = {
      findAll: jest.fn().mockResolvedValue([]),
      create: jest.fn().mockResolvedValue(created),
    };
    const donorRepository = {
      findById: jest.fn().mockResolvedValue(donor),
    };
    const eventBus = { publishAll: jest.fn() } as unknown as EventBus;

    const handler = new CreateDonationHandler(
      donationRepository as any,
      donorRepository as any,
      eventBus,
    );
    const result = await handler.execute(
      new CreateDonationCommand({
        type: DonationType.ONETIME,
        amount: 500,
        donorId: 'donor-1',
      }),
    );

    expect(donationRepository.create).toHaveBeenCalled();
    expect(result.amount).toBe(500);
    expect(eventBus.publishAll).toHaveBeenCalled();
  });

  it('rejects duplicate regular donation for same donor and period', async () => {
    const donationRepository = {
      findAll: jest.fn().mockResolvedValue([{ id: 'existing' }]),
      create: jest.fn(),
    };
    const donorRepository = {
      findById: jest.fn().mockResolvedValue(donor),
    };
    const eventBus = { publishAll: jest.fn() } as unknown as EventBus;
    const handler = new CreateDonationHandler(
      donationRepository as any,
      donorRepository as any,
      eventBus,
    );

    await expect(
      handler.execute(
        new CreateDonationCommand({
          type: DonationType.REGULAR,
          amount: 500,
          donorId: 'donor-1',
          startDate: new Date('2026-07-01'),
          endDate: new Date('2026-07-31'),
        }),
      ),
    ).rejects.toThrow('Donation already exists for this donor in the given period');

    expect(donationRepository.create).not.toHaveBeenCalled();
  });
});
