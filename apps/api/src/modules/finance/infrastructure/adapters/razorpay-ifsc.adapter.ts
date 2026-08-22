import { Injectable, Logger } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { firstValueFrom } from 'rxjs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { IIfscLookupPort, IfscLookupResult } from '../../application/ports/ifsc-lookup.port';
import { normalizeIfsc } from '../../domain/validation/ifsc.validation';

interface RazorpayIfscResponse {
  IFSC?: string;
  BANK?: string;
  BRANCH?: string;
}

interface CacheEntry {
  result: IfscLookupResult;
  expiresAt: number;
}

const CACHE_TTL_MS = 24 * 60 * 60 * 1000;

@Injectable()
export class RazorpayIfscAdapter implements IIfscLookupPort {
  private readonly logger = new Logger(RazorpayIfscAdapter.name);
  private readonly cache = new Map<string, CacheEntry>();
  private readonly baseUrl: string;

  constructor(private readonly httpService: HttpService) {
    this.baseUrl = (process.env.IFSC_API_BASE_URL ?? 'https://ifsc.razorpay.com').replace(/\/$/, '');
  }

  async lookup(ifsc: string): Promise<IfscLookupResult> {
    const normalized = normalizeIfsc(ifsc);
    const cached = this.cache.get(normalized);
    if (cached && cached.expiresAt > Date.now()) {
      return cached.result;
    }

    try {
      const response = await firstValueFrom(
        this.httpService.get<RazorpayIfscResponse>(`${this.baseUrl}/${normalized}`, {
          validateStatus: status => status === 200 || status === 404,
        }),
      );

      if (response.status === 404) {
        throw new BusinessException('Invalid IFSC code', 'IFSC_NOT_FOUND', 404);
      }

      const bankName = response.data.BANK?.trim();
      const branch = response.data.BRANCH?.trim();
      if (!bankName || !branch) {
        throw new BusinessException('Unable to resolve IFSC details', 'IFSC_LOOKUP_FAILED', 502);
      }

      const result: IfscLookupResult = {
        ifsc: response.data.IFSC?.trim() || normalized,
        bankName,
        branch,
      };

      this.cache.set(normalized, { result, expiresAt: Date.now() + CACHE_TTL_MS });
      return result;
    } catch (error) {
      if (error instanceof BusinessException) {
        throw error;
      }
      this.logger.warn(`IFSC lookup failed for ${normalized}: ${error instanceof Error ? error.message : error}`);
      throw new BusinessException('Unable to resolve IFSC details', 'IFSC_LOOKUP_FAILED', 502);
    }
  }
}
