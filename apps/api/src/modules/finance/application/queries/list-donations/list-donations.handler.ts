import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IUserLookupPort } from '@nabarun-ngo/nestjs-shared-core';
import { IDonationRepository } from '../../../domain/repositories/donation.repository';
import { IDonorRepository } from '../../../domain/repositories/donor.repository';
import { DonorType } from '../../../domain/enums/donor-type.enum';
import { DonationMapper } from '../../mappers/donation.mapper';
import { buildDonationDonorEnrichment } from '../../mappers/donation-donor-display.helper';
import { ListDonationsQuery } from './list-donations.query';
import { InvoiceEntityType } from '../../../../invoice/domain/enums/invoice-entity-type.enum';
import { InvoiceFacade } from '../../../../invoice/application/services/invoice.facade';
import { DonationDto } from '../../../presentation/dtos/donation.dto';
import { PagedResponse } from '@nabarun-ngo/nestjs-shared-core';

@QueryHandler(ListDonationsQuery)
@Injectable()
export class ListDonationsHandler implements IQueryHandler<ListDonationsQuery, PagedResponse<DonationDto>> {
  constructor(
    @Inject(IDonationRepository) private readonly donationRepository: IDonationRepository,
    @Inject(IDonorRepository) private readonly donorRepository: IDonorRepository,
    @Inject(IUserLookupPort) private readonly userLookup: IUserLookupPort,
    private readonly invoiceFacade: InvoiceFacade,
  ) { }

  async execute(query: ListDonationsQuery): Promise<PagedResponse<DonationDto>> {
    let donorId = query.filter.donorId;
    const donorType = query.filter.donorType
      ?? (query.filter.isGuest === 'Y'
        ? DonorType.GUEST
        : query.filter.isGuest === 'N'
          ? DonorType.MEMBER
          : undefined);
    if (query.filter.userProfileId && !donorId) {
      const donor = await this.donorRepository.findByUserProfileId(query.filter.userProfileId);
      donorId = donor?.id;
      if (!donorId) {
        return { content: [], totalSize: 0, pageIndex: query.filter.pageIndex ?? 0, pageSize: query.filter.pageSize ?? 20 };
      }
    }

    const page = await this.donationRepository.findPaged({
      pageIndex: query.filter.pageIndex ?? 0,
      pageSize: query.filter.pageSize ?? 20,
      props: {
        donationId: query.filter.donationId,
        donorId,
        status: query.filter.status,
        type: query.filter.type,
        donorType,
        forEventId: query.filter.forEventId,
        startDate_raisedOn: query.filter.startDate,
        endDate_raisedOn: query.filter.endDate,
      },
    });

    const donorIds = [...new Set(page.content.map((d) => d.donorId).filter(Boolean))] as string[];
    const donorMap = new Map(
      (await Promise.all(donorIds.map((id) => this.donorRepository.findById(id))))
        .filter(Boolean)
        .map((d) => [d!.id, d!]),
    );
    const memberIds = [...donorMap.values()]
      .filter((d) => d.type === DonorType.MEMBER && d.userProfileId)
      .map((d) => d.userProfileId!);
    const users = memberIds.length > 0 ? await this.userLookup.findByIds(memberIds) : [];
    const userMap = new Map(users.map((u) => [u.id, u]));
    const invoices = page.content.length
      ? await this.invoiceFacade.findIssuedByEntities(
        InvoiceEntityType.DONATION,
        page.content.map((d) => d.id),
      )
      : [];
    const invoiceMap = new Map(invoices.map((invoice) => [invoice.entityId, invoice]));

    return {
      content: page.content.map((donation) => {
        const donor = donation.donorId ? donorMap.get(donation.donorId) : undefined;
        const userProfile = donor?.userProfileId ? userMap.get(donor.userProfileId) : undefined;
        return DonationMapper.toDto(
          donation,
          buildDonationDonorEnrichment(donor, userProfile),
          invoiceMap.get(donation.id),
        );
      }),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
