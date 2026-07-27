import { Injectable, Logger } from '@nestjs/common';
import { BasePrismaService, CacheService } from '@nabarun-ngo/nestjs-shared-persistence';
import { PrismaClient } from '../../shared/persistence/prisma/client';
import { collectRuntimeMetrics } from './health-metrics.util';

interface LivenessResponse {
  status: 'ok';
  timestamp: string;
}

interface ReadinessCheck {
  database: 'up' | 'down';
  redis: 'up' | 'down';
}

interface ReadinessResponse {
  status: 'ok' | 'error';
  ready: boolean;
  checks: ReadinessCheck;
  timestamp: string;
}

const REDIS_PROBE_KEY = '__health_probe__';

@Injectable()
export class HealthService {
  private readonly logger = new Logger(HealthService.name);

  constructor(
    private readonly prisma: BasePrismaService<PrismaClient>,
    private readonly cache: CacheService,
  ) {}

  getLiveness(): LivenessResponse {
    return {
      status: 'ok',
      timestamp: new Date().toISOString(),
    };
  }

  getMetrics(): ReturnType<typeof collectRuntimeMetrics> {
    return collectRuntimeMetrics();
  }

  async getReadiness(): Promise<ReadinessResponse> {
    const [database, redis] = await Promise.all([
      this.checkDatabase(),
      this.checkRedis(),
    ]);

    const checks: ReadinessCheck = {
      database: database ? 'up' : 'down',
      redis: redis ? 'up' : 'down',
    };
    const ready = database && redis;

    return {
      status: ready ? 'ok' : 'error',
      ready,
      checks,
      timestamp: new Date().toISOString(),
    };
  }

  private async checkDatabase(): Promise<boolean> {
    try {
      await this.prisma.client.$queryRaw`SELECT 1`;
      return true;
    } catch (err) {
      this.logger.warn(
        `Database readiness check failed: ${err instanceof Error ? err.message : String(err)}`,
      );
      return false;
    }
  }

  private async checkRedis(): Promise<boolean> {
    try {
      await this.cache.set(REDIS_PROBE_KEY, '1', 5_000);
      const value = await this.cache.get<string>(REDIS_PROBE_KEY);
      await this.cache.del(REDIS_PROBE_KEY);
      return value === '1';
    } catch (err) {
      this.logger.warn(
        `Redis readiness check failed: ${err instanceof Error ? err.message : String(err)}`,
      );
      return false;
    }
  }
}
