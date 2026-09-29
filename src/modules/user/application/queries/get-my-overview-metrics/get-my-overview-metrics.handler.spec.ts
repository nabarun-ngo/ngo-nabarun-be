import { GetMyOverviewMetricsHandler } from './get-my-overview-metrics.handler';
import { GetMyOverviewMetricsQuery } from './get-my-overview-metrics.query';
import { IUserRepository } from '../../../domain/repositories/user.repository';

describe('GetMyOverviewMetricsHandler', () => {
  let userRepo: jest.Mocked<Pick<IUserRepository, 'getMyOverviewAggregates'>>;
  let handler: GetMyOverviewMetricsHandler;

  beforeEach(() => {
    userRepo = {
      getMyOverviewAggregates: jest.fn().mockResolvedValue({
        pendingDonations: 1000,
        walletBalance: 5000,
        unsettledExpense: 200,
        pendingTask: 2,
      }),
    };
    handler = new GetMyOverviewMetricsHandler(userRepo as unknown as IUserRepository);
  });

  it('returns all metrics when user has every permission', async () => {
    const result = await handler.execute(
      new GetMyOverviewMetricsQuery(
        'user-1',
        ['read:donations', 'read:users', 'read:expenses', 'read:requests'],
        ['MEMBER'],
        [],
      ),
    );

    expect(result).toEqual({
      pendingDonations: 1000,
      walletBalance: 5000,
      unsettledExpense: 200,
      pendingTask: 2,
    });
    expect(userRepo.getMyOverviewAggregates).toHaveBeenCalledWith(
      'user-1',
      ['MEMBER'],
      [],
      ['read:donations', 'read:users', 'read:expenses', 'read:requests'],
    );
  });

  it('omits metrics the user is not permitted to see', async () => {
    const result = await handler.execute(
      new GetMyOverviewMetricsQuery('user-1', ['read:expenses']),
    );

    expect(result).toEqual({ unsettledExpense: 200 });
    expect(userRepo.getMyOverviewAggregates).toHaveBeenCalledWith(
      'user-1',
      [],
      [],
      ['read:expenses'],
    );
  });

  it('returns empty object when user has no relevant permissions', async () => {
    const result = await handler.execute(
      new GetMyOverviewMetricsQuery('user-1', ['read:notifications']),
    );

    expect(result).toEqual({});
    expect(userRepo.getMyOverviewAggregates).not.toHaveBeenCalled();
  });

  it('returns pendingTask when user has read:requests', async () => {
    const result = await handler.execute(
      new GetMyOverviewMetricsQuery('user-1', ['read:requests'], ['SECRETARY'], ['OPS']),
    );

    expect(result).toEqual({ pendingTask: 2 });
    expect(userRepo.getMyOverviewAggregates).toHaveBeenCalledWith(
      'user-1',
      ['SECRETARY'],
      ['OPS'],
      ['read:requests'],
    );
  });
});
