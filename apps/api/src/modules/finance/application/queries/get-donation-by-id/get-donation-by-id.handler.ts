import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { IDonationRepository } from '../../../domain/repositories/donation.repository';
import { InvoiceEntityType } from '../../../../invoice/domain/enums/invoice-entity-type.enum';
import { InvoiceFacade } from '../../../../invoice/application/services/invoice.facade';
import { DonationDto } from '../../dtos/donation.dto';
import { DonationMapper } from '../../mappers/donation.mapper';
import { GetDonationByIdQuery } from './get-donation-by-id.query';

@QueryHandler(GetDonationByIdQuery)
@Injectable()
export class GetDonationByIdHandler implements IQueryHandler<GetDonationByIdQuery, DonationDto> {
  constructor(
    @Inject(IDonationRepository) private readonly repo: IDonationRepository,
    private readonly invoiceFacade: InvoiceFacade,
  ) { }

  async execute(query: GetDonationByIdQuery): Promise<DonationDto> {
    const donation = await this.repo.findById(query.id);
    if (!donation) throw new BusinessException('Donation not found with id: ' + query.id);
    const invoice = await this.invoiceFacade.findIssuedByEntity(
      InvoiceEntityType.DONATION,
      donation.id,
    );
    return DonationMapper.toDto(donation, undefined, invoice ?? undefined);
  }
}
