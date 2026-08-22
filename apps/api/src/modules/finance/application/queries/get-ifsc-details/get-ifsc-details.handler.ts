import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { IIfscLookupPort } from '../../ports/ifsc-lookup.port';
import { IfscDetailsDto } from '../../../presentation/dtos/ifsc.dto';
import { assertValidIfscFormat } from '../../../domain/validation/ifsc.validation';
import { GetIfscDetailsQuery } from './get-ifsc-details.query';

@QueryHandler(GetIfscDetailsQuery)
@Injectable()
export class GetIfscDetailsHandler implements IQueryHandler<GetIfscDetailsQuery, IfscDetailsDto> {
  constructor(@Inject(IIfscLookupPort) private readonly ifscLookup: IIfscLookupPort) {}

  async execute({ ifsc }: GetIfscDetailsQuery): Promise<IfscDetailsDto> {
    let normalized: string;
    try {
      normalized = assertValidIfscFormat(ifsc);
    } catch {
      throw new BusinessException('Invalid IFSC format', 'INVALID_IFSC_FORMAT', 400);
    }

    try {
      const result = await this.ifscLookup.lookup(normalized);
      return {
        ifsc: result.ifsc,
        bankName: result.bankName,
        branch: result.branch,
      };
    } catch (error) {
      if (error instanceof BusinessException) {
        throw error;
      }
      const message = error instanceof Error ? error.message : 'IFSC lookup failed';
      if (message.includes('not found') || message.includes('404')) {
        throw new BusinessException('Invalid IFSC code', 'IFSC_NOT_FOUND', 404);
      }
      throw new BusinessException('Unable to resolve IFSC details', 'IFSC_LOOKUP_FAILED', 502);
    }
  }
}
