import { GetMyOverviewMetricsHandler } from './get-my-overview-metrics.handler';
import { GetMyOverviewMetricsQuery } from './get-my-overview-metrics.query';
import { IUserRepository } from '../../../domain/repositories/user.repository';

const aggregates = {
  pendingDonations: 1000,
  walletBalance: 5000,
  unsettledExpense: 200,
  pendingTask: 2,
};

describe('GetMyOverviewMetricsHandler', () => {
  let userRepo: jest.Mocked<Pick<IUserRepository, 'getMyOverviewAggregates'>>;
  let handler: GetMyOverviewMetricsHandler;

  beforeEach(() => {
    userRepo = {
      getMyOverviewAggregates: jest.fn().mockResolvedValue(aggregates),
    };
    handler = new GetMyOverviewMetricsHandler(userRepo as unknown as IUserRepository);
  });

  it('returns every aggregate from one repository read', async () => {
    const result = await handler.execute(
      new GetMyOverviewMetricsQuery(
        'user-1',
        ['read:donations', 'read:users', 'read:expenses', 'read:requests'],
        ['MEMBER'],
        [],
      ),
    );

    expect(result).toEqual(aggregates);
    expect(userRepo.getMyOverviewAggregates).toHaveBeenCalledWith(
      'user-1',
      ['MEMBER'],
      [],
      ['read:donations', 'read:users', 'read:expenses', 'read:requests'],
    );
  });

  it('still returns every aggregate when only one permission is present', async () => {
    const result = await handler.execute(
      new GetMyOverviewMetricsQuery('user-1', ['read:expenses']),
    );

    expect(result).toEqual(aggregates);
    expect(userRepo.getMyOverviewAggregates).toHaveBeenCalledWith(
      'user-1',
      [],
      [],
      ['read:expenses'],
    );
  });

  it('still reads aggregates when the user has no overview permissions', async () => {
    const result = await handler.execute(
      new GetMyOverviewMetricsQuery('user-1', ['read:notifications']),
    );

    expect(result).toEqual(aggregates);
    expect(userRepo.getMyOverviewAggregates).toHaveBeenCalledWith(
      'user-1',
      [],
      [],
      ['read:notifications'],
    );
  });

  it('passes roles and role groups through to the repository', async () => {
    const result = await handler.execute(
      new GetMyOverviewMetricsQuery('user-1', ['read:requests'], ['SECRETARY'], ['OPS']),
    );

    expect(result).toEqual(aggregates);
    expect(userRepo.getMyOverviewAggregates).toHaveBeenCalledWith(
      'user-1',
      ['SECRETARY'],
      ['OPS'],
      ['read:requests'],
    );
  });
});
