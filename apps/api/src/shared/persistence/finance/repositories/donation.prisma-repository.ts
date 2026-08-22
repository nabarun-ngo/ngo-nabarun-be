import { Injectable } from '@nestjs/common';
import { BasePrismaService } from '@nabarun-ngo/nestjs-shared-persistence';
import { BaseFilter, Page } from '@nabarun-ngo/nestjs-shared-core';
import { Prisma, PrismaClient } from '../../prisma/client';
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

@Injectable()
export class DonationPrismaRepository implements IDonationRepository {
  constructor(private readonly database: BasePrismaService<PrismaClient>) { }

  private readonly include = {
    donor: { include: { userProfile: { include: { phoneNumbers: true } } } },
    paidToAccount: true,
    confirmedBy: true,
    activity: true,
  } as const;

  async count(filter: DonationFilter): Promise<number> {
    return await this.database.client.donation.count({ where: this.whereQuery(filter) });
  }

  async findPaged(filter?: BaseFilter<DonationFilter>): Promise<Page<Donation>> {
    const where = this.whereQuery(filter?.props);
    const orderBy = filter?.props?.donorType === DonorType.GUEST
      ? { raisedOn: 'desc' as const }
      : { startDate: 'desc' as const };

    const [data, total] = await Promise.all([
      this.database.client.donation.findMany({
        where,
        orderBy,
        include: this.include,
        skip: (filter?.pageIndex ?? 0) * (filter?.pageSize ?? 1000),
        take: filter?.pageSize ?? 1000,
      }),
      this.database.client.donation.count({ where }),
    ]);
    return new Page<Donation>(
      data.map(m => DonationPrismaMapper.toDonationDomain(m)!),
      total,
      filter?.pageIndex ?? 0,
      filter?.pageSize ?? 1000,
    );
  }

  async findAll(filter?: DonationFilter): Promise<Donation[]> {
    const donations = await this.database.client.donation.findMany({
      where: this.whereQuery(filter),
      orderBy: { raisedOn: 'desc' },
      include: this.include,
    });
    return donations.map((m) => DonationPrismaMapper.toDonationDomain(m)!);
  }

  private whereQuery(props?: DonationFilter): Prisma.DonationWhereInput {
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

  async findById(id: string): Promise<Donation | null> {
    const donation = await this.database.client.donation.findUnique({
      where: { id },
      include: this.include,
    });
    return DonationPrismaMapper.toDonationDomain(donation as FullDonation);
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

  async create(donation: Donation): Promise<Donation> {
    const created = await this.database.client.donation.create({
      data: DonationPrismaMapper.toDonationCreatePersistence(donation),
      include: this.include,
    });
    return DonationPrismaMapper.toDonationDomain(created)!;
  }

  async update(id: string, donation: Donation): Promise<Donation> {
    const updated = await this.database.client.donation.update({
      where: { id },
      data: DonationPrismaMapper.toDonationUpdatePersistence(donation),
      include: this.include,
    });
    return DonationPrismaMapper.toDonationDomain(updated)!;
  }

  async delete(id: string): Promise<void> {
    await this.database.client.donation.update({
      where: { id },
      data: { deletedAt: new Date() },
    });
  }
}
