import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { Prisma, PrismaClient } from '../../prisma/client';
import type {
  DonorWhereInput,
  DonorWhereUniqueInput,
  DonorUncheckedCreateInput,
  DonorUncheckedUpdateInput,
  DonorOrderByWithRelationInput,
} from '../../prisma/models/Donor';
import { Donor } from '../../../../modules/finance/domain/aggregates/donor/donor.aggregate';
import { DonorStatus } from '../../../../modules/finance/domain/enums/donor-status.enum';
import { DonorType } from '../../../../modules/finance/domain/enums/donor-type.enum';
import { DonorFilter, IDonorRepository } from '../../../../modules/finance/domain/repositories/donor.repository';
import { DonorPrismaMapper } from '../mapper/donor-prisma.mapper';

export type FullDonor = Prisma.DonorGetPayload<{
  include: { userProfile: { include: { phoneNumbers: true } } };
}>;

const DONOR_RELATIONS = {
  userProfile: { include: { phoneNumbers: true } },
} as const;

@Injectable()
export class DonorPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'donor',
    Donor,
    string,
    DonorFilter,
    FullDonor,
    DonorWhereInput,
    DonorWhereUniqueInput,
    DonorUncheckedCreateInput,
    DonorUncheckedUpdateInput,
    DonorOrderByWithRelationInput,
    typeof DONOR_RELATIONS
  >
  implements IDonorRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'donor');
  }

  async findById(id: string): Promise<Donor | null> {
    const donor = await this.delegate.findUnique({
      where: { id },
      include: DONOR_RELATIONS,
    });
    return DonorPrismaMapper.toDomain(donor);
  }

  async findByUserProfileId(userProfileId: string): Promise<Donor | null> {
    const donor = await this.delegate.findUnique({
      where: { userProfileId },
      include: DONOR_RELATIONS,
    });
    return DonorPrismaMapper.toDomain(donor);
  }

  async findByEmail(email: string): Promise<Donor | null> {
    const donor = await this.delegate.findUnique({
      where: { email },
      include: DONOR_RELATIONS,
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
    const data = await this.delegate.findMany({
      where: {
        status: { in: [DonorStatus.PAUSED, DonorStatus.WAIVED] },
        statusEndDate: { lte: asOf },
        deletedAt: null,
      },
      include: DONOR_RELATIONS,
    });
    return data.map((d) => DonorPrismaMapper.toDomain(d)!);
  }

  async reassignDonations(fromDonorId: string, toDonorId: string): Promise<void> {
    await this.client.donation.updateMany({
      where: { donorId: fromDonorId },
      data: { donorId: toDonorId },
    });
  }

  async create(donor: Donor): Promise<Donor> {
    const created = await this.delegate.create({
      data: DonorPrismaMapper.toCreateInput(donor),
      include: DONOR_RELATIONS,
    });
    return DonorPrismaMapper.toDomain(created)!;
  }

  async update(id: string, donor: Donor): Promise<Donor> {
    const updated = await this.delegate.update({
      where: { id },
      data: DonorPrismaMapper.toUpdateInput(donor),
      include: DONOR_RELATIONS,
    });
    return DonorPrismaMapper.toDomain(updated)!;
  }

  protected toDomain(row: FullDonor): Donor {
    return DonorPrismaMapper.toDomain(row)!;
  }

  protected toCreateInput(donor: Donor): DonorUncheckedCreateInput {
    return DonorPrismaMapper.toCreateInput(donor);
  }

  protected toUpdateInput(_id: string, donor: Donor): DonorUncheckedUpdateInput {
    return DonorPrismaMapper.toUpdateInput(donor);
  }

  protected toUniqueWhere(id: string): DonorWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(props?: DonorFilter): DonorWhereInput {
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

  protected override supportsSoftDelete(): boolean {
    return true;
  }

  protected override defaultOrderBy(): DonorOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected override toInclude(): typeof DONOR_RELATIONS {
    return DONOR_RELATIONS;
  }
}
