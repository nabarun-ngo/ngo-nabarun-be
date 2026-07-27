import { Controller, Get, HttpStatus, Res } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '@nabarun-ngo/nestjs-shared-auth';
import { Response } from 'express';
import { HealthService } from './health.service';

@ApiTags('Health')
@Controller()
@Public()
export class HealthController {
  constructor(private readonly healthService: HealthService) {}

  @Get('health')
  @ApiOperation({ summary: 'Liveness probe — process is running' })
  getLiveness() {
    return this.healthService.getLiveness();
  }

  @Get('ready')
  @ApiOperation({ summary: 'Readiness probe — database and Redis are reachable' })
  async getReadiness(@Res({ passthrough: true }) res: Response) {
    const result = await this.healthService.getReadiness();
    if (!result.ready) {
      res.status(HttpStatus.SERVICE_UNAVAILABLE);
    }
    return result;
  }

  @Get('metrics')
  @ApiOperation({ summary: 'Runtime metrics — memory, CPU, uptime, and load average' })
  getMetrics() {
    return this.healthService.getMetrics();
  }
}
