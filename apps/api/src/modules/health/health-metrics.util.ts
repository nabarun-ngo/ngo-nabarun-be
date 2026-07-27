import os from 'os';

interface ProcessMemoryMetrics {
  rssBytes: number;
  rssMb: number;
  heapTotalBytes: number;
  heapTotalMb: number;
  heapUsedBytes: number;
  heapUsedMb: number;
  externalBytes: number;
  externalMb: number;
  arrayBuffersBytes: number;
  arrayBuffersMb: number;
  heapUsedPercent: number;
}

interface SystemMemoryMetrics {
  totalBytes: number;
  totalMb: number;
  freeBytes: number;
  freeMb: number;
  usedBytes: number;
  usedMb: number;
  usedPercent: number;
}

interface CpuMetrics {
  userMicros: number;
  systemMicros: number;
  userMs: number;
  systemMs: number;
}

interface MetricsResponse {
  status: 'ok';
  timestamp: string;
  uptimeSeconds: number;
  pid: number;
  nodeVersion: string;
  platform: NodeJS.Platform;
  arch: string;
  cpuCount: number;
  memory: {
    process: ProcessMemoryMetrics;
    system: SystemMemoryMetrics;
  };
  cpu: CpuMetrics;
  loadAverage: {
    oneMinute: number;
    fiveMinutes: number;
    fifteenMinutes: number;
  };
}

function bytesToMb(bytes: number): number {
  return Math.round((bytes / (1024 * 1024)) * 100) / 100;
}

function percent(part: number, total: number): number {
  if (total <= 0) return 0;
  return Math.round((part / total) * 10_000) / 100;
}

export function collectRuntimeMetrics(): MetricsResponse {
  const memoryUsage = process.memoryUsage();
  const cpuUsage = process.cpuUsage();
  const totalMem = os.totalmem();
  const freeMem = os.freemem();
  const usedMem = totalMem - freeMem;
  const [oneMinute, fiveMinutes, fifteenMinutes] = os.loadavg();

  return {
    status: 'ok',
    timestamp: new Date().toISOString(),
    uptimeSeconds: Math.round(process.uptime() * 100) / 100,
    pid: process.pid,
    nodeVersion: process.version,
    platform: process.platform,
    arch: process.arch,
    cpuCount: os.cpus().length,
    memory: {
      process: {
        rssBytes: memoryUsage.rss,
        rssMb: bytesToMb(memoryUsage.rss),
        heapTotalBytes: memoryUsage.heapTotal,
        heapTotalMb: bytesToMb(memoryUsage.heapTotal),
        heapUsedBytes: memoryUsage.heapUsed,
        heapUsedMb: bytesToMb(memoryUsage.heapUsed),
        externalBytes: memoryUsage.external,
        externalMb: bytesToMb(memoryUsage.external),
        arrayBuffersBytes: memoryUsage.arrayBuffers,
        arrayBuffersMb: bytesToMb(memoryUsage.arrayBuffers),
        heapUsedPercent: percent(memoryUsage.heapUsed, memoryUsage.heapTotal),
      },
      system: {
        totalBytes: totalMem,
        totalMb: bytesToMb(totalMem),
        freeBytes: freeMem,
        freeMb: bytesToMb(freeMem),
        usedBytes: usedMem,
        usedMb: bytesToMb(usedMem),
        usedPercent: percent(usedMem, totalMem),
      },
    },
    cpu: {
      userMicros: cpuUsage.user,
      systemMicros: cpuUsage.system,
      userMs: Math.round(cpuUsage.user / 1000),
      systemMs: Math.round(cpuUsage.system / 1000),
    },
    loadAverage: {
      oneMinute,
      fiveMinutes,
      fifteenMinutes,
    },
  };
}
