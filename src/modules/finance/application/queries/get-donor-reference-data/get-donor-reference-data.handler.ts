import { Inject, Injectable, Optional } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { DonorRefDataDto } from '../../dtos/donor.dto';
import { IFinanceReferenceDataPort } from '../../ports/finance-reference-data.port';
import { GetDonorReferenceDataQuery } from './get-donor-reference-data.query';

@QueryHandler(GetDonorReferenceDataQuery)
@Injectable()
export class GetDonorReferenceDataHandler
  implements IQueryHandler<GetDonorReferenceDataQuery, DonorRefDataDto>
{
  constructor(
    @Optional()
    @Inject(IFinanceReferenceDataPort)
    private readonly port: IFinanceReferenceDataPort,
  ) {}

  async execute(): Promise<DonorRefDataDto> {
    if (!this.port) {
      return {};
    }
    const data = await this.port.getDonorReferenceData();
    return {
      donorStatuses: data.donorStatuses,
      memberEditableDonorStatuses: data.memberEditableDonorStatuses,
      statusesRequiringEndDate: data.statusesRequiringEndDate,
    };
  }
}
