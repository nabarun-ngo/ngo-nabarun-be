import { collectRuntimeMetrics } from './health-metrics.util';

describe('collectRuntimeMetrics', () => {
  it('returns process and system memory metrics', () => {
    const metrics = collectRuntimeMetrics();

    expect(metrics.status).toBe('ok');
    expect(metrics.timestamp).toBeDefined();
    expect(metrics.uptimeSeconds).toBeGreaterThanOrEqual(0);
    expect(metrics.pid).toBe(process.pid);
    expect(metrics.nodeVersion).toBe(process.version);
    expect(metrics.cpuCount).toBeGreaterThan(0);

    expect(metrics.memory.process.rssBytes).toBeGreaterThan(0);
    expect(metrics.memory.process.heapUsedBytes).toBeGreaterThan(0);
    expect(metrics.memory.process.heapUsedMb).toBeGreaterThan(0);
    expect(metrics.memory.process.heapUsedPercent).toBeGreaterThanOrEqual(0);

    expect(metrics.memory.system.totalBytes).toBeGreaterThan(0);
    expect(metrics.memory.system.usedBytes).toBeGreaterThan(0);
    expect(metrics.memory.system.usedPercent).toBeGreaterThan(0);
    expect(metrics.memory.system.usedPercent).toBeLessThanOrEqual(100);
  });

  it('returns cpu usage and load average', () => {
    const metrics = collectRuntimeMetrics();

    expect(metrics.cpu.userMicros).toBeGreaterThanOrEqual(0);
    expect(metrics.cpu.systemMicros).toBeGreaterThanOrEqual(0);
    expect(metrics.loadAverage.oneMinute).toBeGreaterThanOrEqual(0);
    expect(metrics.loadAverage.fiveMinutes).toBeGreaterThanOrEqual(0);
    expect(metrics.loadAverage.fifteenMinutes).toBeGreaterThanOrEqual(0);
  });
});
