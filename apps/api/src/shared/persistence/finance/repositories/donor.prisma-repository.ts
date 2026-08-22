import { Injectable } from '@nestjs/common';
import { BasePrismaService } from '@nabarun-ngo/nestjs-shared-persistence';
import { BaseFilter, Page } from '@nabarun-ngo/nestjs-shared-core';
import { Prisma, PrismaClient } from '../../prisma/client';
import { Donor } from '../../../../modules/finance/domain/aggregates/donor/donor.aggregate';
import { DonorStatus } from '../../../../modules/finance/domain/enums/donor-status.enum';
import { DonorType } from '../../../../modules/finance/domain/enums/donor-type.enum';
import { DonorFilter, IDonorRepository } from '../../../../modules/finance/domain/repositories/donor.repository';
import { DonorPrismaMapper } from '../mapper/donor-prisma.mapper';

export type FullDonor = Prisma.DonorGetPayload<{
  include: { userProfile: { include: { phoneNumbers: true } } };
}>;

@Injectable()
export class DonorPrismaRepository implements IDonorRepository {
  constructor(private readonly database: BasePrismaService<PrismaClient>) {}

  async count(filter: DonorFilter): Promise<number> {
    return this.database.client.donor.count({ where: this.whereQuery(filter) });
  }

  async findPaged(filter?: BaseFilter<DonorFilter>): Promise<Page<Donor>> {
    const where = this.whereQuery(filter?.props);
    const [data, total] = await Promise.all([
      this.database.client.donor.findMany({
        where,
        orderBy: { createdAt: 'desc' },
        include: { userProfile: { include: { phoneNumbers: true } } },
        skip: (filter?.pageIndex ?? 0) * (filter?.pageSize ?? 1000),
        take: filter?.pageSize ?? 1000,
      }),
      this.database.client.donor.count({ where }),
    ]);
    return new Page(
      data.map((d) => DonorPrismaMapper.toDomain(d)!),
      total,
      filter?.pageIndex ?? 0,
      filter?.pageSize ?? 1000,
    );
  }

  async findAll(filter?: DonorFilter): Promise<Donor[]> {
    const data = await this.database.client.donor.findMany({
      where: this.whereQuery(filter),
      orderBy: { createdAt: 'desc' },
      include: { userProfile: { include: { phoneNumbers: true } } },
    });
    return data.map((d) => DonorPrismaMapper.toDomain(d)!);
  }

  async findById(id: string): Promise<Donor | null> {
    const donor = await this.database.client.donor.findUnique({
      where: { id },
      include: { userProfile: { include: { phoneNumbers: true } } },
    });
    return DonorPrismaMapper.toDomain(donor);
  }

  async findByUserProfileId(userProfileId: string): Promise<Donor | null> {
    const donor = await this.database.client.donor.findUnique({
      where: { userProfileId },
      include: { userProfile: { include: { phoneNumbers: true } } },
    });
    return DonorPrismaMapper.toDomain(donor);
  }

  async findByEmail(email: string): Promise<Donor | null> {
    const donor = await this.database.client.donor.findUnique({
      where: { email },
      include: { userProfile: { include: { phoneNumbers: true } } },
    });
    return DonorPrismaMapper.toDomain(donor);
  }

  async findScheduleCandidates(donorId?: string): Promise<Donor[]> {
    return this.findAll({
      id: donorId,
      status: [DonorStatus.ACTIVE, DonorStatus.PAUSED],
    });
  }

  async findDueForReactivation(asOf: Date): Promise<Donor[]> {
    const data = await this.database.client.donor.findMany({
      where: {
        status: { in: [DonorStatus.PAUSED, DonorStatus.WAIVED] },
        statusEndDate: { lte: asOf },
        deletedAt: null,
      },
      include: { userProfile: { include: { phoneNumbers: true } } },
    });
    return data.map((d) => DonorPrismaMapper.toDomain(d)!);
  }

  async reassignDonations(fromDonorId: string, toDonorId: string): Promise<void> {
    await this.database.client.donation.updateMany({
      where: { donorId: fromDonorId },
      data: { donorId: toDonorId },
    });
  }

  async create(donor: Donor): Promise<Donor> {
    const created = await this.database.client.donor.create({
      data: DonorPrismaMapper.toCreateInput(donor),
      include: { userProfile: { include: { phoneNumbers: true } } },
    });
    return DonorPrismaMapper.toDomain(created)!;
  }

  async update(id: string, donor: Donor): Promise<Donor> {
    const updated = await this.database.client.donor.update({
      where: { id },
      data: DonorPrismaMapper.toUpdateInput(donor),
      include: { userProfile: { include: { phoneNumbers: true } } },
    });
    return DonorPrismaMapper.toDomain(updated)!;
  }

  async delete(id: string): Promise<void> {
    await this.database.client.donor.update({
      where: { id },
      data: { deletedAt: new Date() },
    });
  }

  private whereQuery(props?: DonorFilter): Prisma.DonorWhereInput {
    const q = props?.q?.trim();
    const qOr: Prisma.DonorWhereInput[] | undefined = q
      ? [
          { fullName: { contains: q, mode: 'insensitive' } },
          { email: { contains: q, mode: 'insensitive' } },
          { phoneNumber: { contains: q, mode: 'insensitive' } },
          {
            userProfile: {
              OR: [
                { firstName: { contains: q, mode: 'insensitive' } },
                { lastName: { contains: q, mode: 'insensitive' } },
                { email: { contains: q, mode: 'insensitive' } },
              ],
            },
          },
        ]
      : undefined;

    return {
      ...(props?.id ? { id: props.id } : {}),
      ...(props?.type ? { type: props.type } : {}),
      ...(props?.userProfileId ? { userProfileId: props.userProfileId } : {}),
      ...(props?.email ? { email: props.email } : {}),
      ...(props?.status && props.status.length > 0 ? { status: { in: props.status } } : {}),
      ...(props?.statusEndDate_lte ? { statusEndDate: { lte: props.statusEndDate_lte } } : {}),
      ...(qOr ? { OR: qOr } : {}),
      deletedAt: null,
    };
  }
}
