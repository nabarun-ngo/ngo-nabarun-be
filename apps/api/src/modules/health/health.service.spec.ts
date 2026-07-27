import { HealthService } from './health.service';

describe('HealthService', () => {
  const prisma = {
    client: {
      $queryRaw: jest.fn().mockResolvedValue([{ '?column?': 1 }]),
    },
  };
  const cache = {
    set: jest.fn().mockResolvedValue(undefined),
    get: jest.fn().mockResolvedValue('1'),
    del: jest.fn().mockResolvedValue(undefined),
  };

  let service: HealthService;

  beforeEach(() => {
    jest.clearAllMocks();
    service = new HealthService(prisma as any, cache as any);
  });

  it('returns ok for liveness', () => {
    const result = service.getLiveness();
    expect(result.status).toBe('ok');
    expect(result.timestamp).toBeDefined();
  });

  it('returns runtime metrics', () => {
    const result = service.getMetrics();

    expect(result.status).toBe('ok');
    expect(result.memory.process.heapUsedMb).toBeGreaterThan(0);
    expect(result.memory.system.totalMb).toBeGreaterThan(0);
    expect(result.cpuCount).toBeGreaterThan(0);
  });

  it('returns ready when database and redis checks pass', async () => {
    const result = await service.getReadiness();

    expect(result.ready).toBe(true);
    expect(result.status).toBe('ok');
    expect(result.checks).toEqual({ database: 'up', redis: 'up' });
    expect(prisma.client.$queryRaw).toHaveBeenCalled();
    expect(cache.set).toHaveBeenCalled();
    expect(cache.del).toHaveBeenCalled();
  });

  it('returns not ready when database check fails', async () => {
    prisma.client.$queryRaw.mockRejectedValueOnce(new Error('db down'));

    const result = await service.getReadiness();

    expect(result.ready).toBe(false);
    expect(result.status).toBe('error');
    expect(result.checks.database).toBe('down');
    expect(result.checks.redis).toBe('up');
  });

  it('returns not ready when redis probe does not round-trip', async () => {
    cache.get.mockResolvedValueOnce(undefined);

    const result = await service.getReadiness();

    expect(result.ready).toBe(false);
    expect(result.checks.redis).toBe('down');
  });
});
