import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { BaseFilter, Page, SortOrder } from '@nabarun-ngo/nestjs-shared-core';
import { Prisma, PrismaClient } from '../../prisma/client';
import type {
  DonationWhereInput,
  DonationWhereUniqueInput,
  DonationUncheckedCreateInput,
  DonationUncheckedUpdateInput,
  DonationOrderByWithRelationInput,
} from '../../prisma/models/Donation';
import { IDonationRepository, DonationFilter } from '../../../../modules/finance/domain/repositories/donation.repository';
import { Donation } from '../../../../modules/finance/domain/aggregates/donation/donation.aggregate';
import { DonationStatus } from '../../../../modules/finance/domain/enums/donation-status.enum';
import { DonationType } from '../../../../modules/finance/domain/enums/donation-type.enum';
import { DonorType } from '../../../../modules/finance/domain/enums/donor-type.enum';
import { DonationPrismaMapper } from '../mapper/donation-prisma.mapper';

export type FullDonation = Prisma.DonationGetPayload<{
  include: {
    donor: { include: { userProfile: { include: { phoneNumbers: true } } } };
    paidToAccount: true;
    confirmedBy: true;
    activity: true;
  };
}>;

const DONATION_RELATIONS = {
  donor: { include: { userProfile: { include: { phoneNumbers: true } } } },
  paidToAccount: true,
  confirmedBy: true,
  activity: true,
} as const;

@Injectable()
export class DonationPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'donation',
    Donation,
    string,
    DonationFilter,
    FullDonation,
    DonationWhereInput,
    DonationWhereUniqueInput,
    DonationUncheckedCreateInput,
    DonationUncheckedUpdateInput,
    DonationOrderByWithRelationInput,
    typeof DONATION_RELATIONS
  >
  implements IDonationRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'donation');
  }

  protected toDomain(row: FullDonation): Donation {
    return DonationPrismaMapper.toDonationDomain(row)!;
  }

  protected toCreateInput(donation: Donation): DonationUncheckedCreateInput {
    return DonationPrismaMapper.toDonationCreatePersistence(donation);
  }

  protected toUpdateInput(_id: string, donation: Donation): DonationUncheckedUpdateInput {
    return DonationPrismaMapper.toDonationUpdatePersistence(donation);
  }

  protected toUniqueWhere(id: string): DonationWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(props?: DonationFilter): DonationWhereInput {
    return {
      ...(props?.type && props.type.length > 0 ? { type: { in: props.type } } : {}),
      ...(props?.status && props.status.length > 0 ? { status: { in: props.status } } : {}),
      ...(props?.forEventId ? { forEventId: props.forEventId } : {}),
      ...(props?.donorId ? { donorId: props.donorId } : {}),
      ...(props?.donorType ? { donor: { type: props.donorType } } : {}),
      ...(props?.donationId ? { id: props.donationId } : {}),
      ...(props?.startDate_raisedOn || props?.endDate_raisedOn
        ? {
          raisedOn: {
            ...(props.startDate_raisedOn ? { gte: props.startDate_raisedOn } : {}),
            ...(props.endDate_raisedOn ? { lte: props.endDate_raisedOn } : {}),
          },
        }
        : {}),
      ...(props?.startDate_confirmedOn || props?.endDate_confirmedOn
        ? {
          confirmedOn: {
            ...(props.startDate_confirmedOn ? { gte: props.startDate_confirmedOn } : {}),
            ...(props.endDate_confirmedOn ? { lte: props.endDate_confirmedOn } : {}),
          },
        }
        : {}),
      ...(props?.startDate_paidOn || props?.endDate_paidOn
        ? {
          paidOn: {
            ...(props.startDate_paidOn ? { gte: props.startDate_paidOn } : {}),
            ...(props.endDate_paidOn ? { lte: props.endDate_paidOn } : {}),
          },
        }
        : {}),
      ...(props?.startDate_lte ? {
        OR: [
          { startDate: { lte: props.startDate_lte } },
          { AND: [{ startDate: null }, { raisedOn: { lte: props.startDate_lte } }] },
        ],
      } : {}),
      ...(props?.endDate_gte ? { endDate: { gte: props.endDate_gte } } : {}),
      deletedAt: null,
    };
  }

  protected override defaultOrderBy(): DonationOrderByWithRelationInput {
    return { raisedOn: 'desc' };
  }

  protected override toInclude(): typeof DONATION_RELATIONS {
    return DONATION_RELATIONS;
  }

  protected override defaultPageSize(): number {
    return 1000;
  }

  /** Guest donations have no schedule, so they are ordered by the date they were raised. */
  override async findPaged(filter?: BaseFilter<DonationFilter>): Promise<Page<Donation>> {
    const sortBy = filter?.props?.donorType === DonorType.GUEST ? 'raisedOn' : 'startDate';
    return super.findPaged(
      new BaseFilter<DonationFilter>(
        filter?.props,
        filter?.pageIndex,
        filter?.pageSize,
        sortBy,
        SortOrder.DESC,
      ),
    );
  }

  async findByDonorId(donorId: string): Promise<Donation[]> {
    return this.findAll({ donorId });
  }

  async findByStatus(status: DonationStatus): Promise<Donation[]> {
    return this.findAll({ status: [status] });
  }

  async findByType(type: DonationType): Promise<Donation[]> {
    return this.findAll({ type: [type] });
  }

  async findPendingRegularDonations(): Promise<Donation[]> {
    return this.findAll({ type: [DonationType.REGULAR], status: [DonationStatus.RAISED] });
  }

  async findByDateRange(startDate: Date, endDate: Date): Promise<Donation[]> {
    return this.findAll({ startDate_raisedOn: startDate, endDate_raisedOn: endDate });
  }

  override async create(donation: Donation): Promise<Donation> {
    const created = await this.delegate.create({
      data: DonationPrismaMapper.toDonationCreatePersistence(donation),
      include: DONATION_RELATIONS,
    });
    return DonationPrismaMapper.toDonationDomain(created)!;
  }

  override async update(id: string, donation: Donation): Promise<Donation> {
    const updated = await this.delegate.update({
      where: { id },
      data: DonationPrismaMapper.toDonationUpdatePersistence(donation),
      include: DONATION_RELATIONS,
    });
    return DonationPrismaMapper.toDonationDomain(updated)!;
  }

  override async delete(id: string): Promise<void> {
    await this.delegate.update({
      where: { id },
      data: { deletedAt: new Date() },
    });
  }
}
