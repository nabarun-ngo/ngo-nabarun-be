import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { Prisma, PrismaClient } from '../../prisma/client';
import type {
  EarningWhereInput,
  EarningWhereUniqueInput,
  EarningUncheckedCreateInput,
  EarningUncheckedUpdateInput,
  EarningOrderByWithRelationInput,
} from '../../prisma/models/Earning';
import { IEarningRepository, EarningFilter } from '../../../../modules/finance/domain/repositories/earning.repository';
import { Earning } from '../../../../modules/finance/domain/aggregates/earning/earning.aggregate';
import { EarningPrismaMapper } from '../mapper/earning-prisma.mapper';

export type EarningPersistence = Prisma.EarningGetPayload<{
  include: {
    account: true;
    createdBy: true;
    receivedBy: true;
  }
}>;

const EARNING_RELATIONS = {
  account: true,
  createdBy: true,
  receivedBy: true,
} as const;

@Injectable()
export class EarningPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'earning',
    Earning,
    string,
    EarningFilter,
    EarningPersistence,
    EarningWhereInput,
    EarningWhereUniqueInput,
    EarningUncheckedCreateInput,
    EarningUncheckedUpdateInput,
    EarningOrderByWithRelationInput,
    typeof EARNING_RELATIONS
  >
  implements IEarningRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'earning');
  }

  protected toDomain(row: EarningPersistence): Earning {
    return EarningPrismaMapper.toEarningDomain(row)!;
  }

  protected toCreateInput(earning: Earning): EarningUncheckedCreateInput {
    return EarningPrismaMapper.toEarningCreatePersistence(earning);
  }

  protected toUpdateInput(_id: string, earning: Earning): EarningUncheckedUpdateInput {
    return EarningPrismaMapper.toEarningUpdatePersistence(earning);
  }

  protected toUniqueWhere(id: string): EarningWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(props?: EarningFilter): EarningWhereInput {
    return {
      ...(props?.category ? { category: { in: props.category } } : {}),
      ...(props?.source ? { source: props.source } : {}),
      ...(props?.status ? { status: { in: props.status } } : {}),
      ...(props?.accountId ? { accountId: props.accountId } : {}),
      ...(props?.startDate || props?.endDate
        ? {
          earningDate: {
            ...(props.startDate ? { gte: props.startDate } : {}),
            ...(props.endDate ? { lte: props.endDate } : {}),
          },
        }
        : {}),
      deletedAt: null,
    };
  }

  protected override defaultOrderBy(): EarningOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected override toInclude(): typeof EARNING_RELATIONS {
    return EARNING_RELATIONS;
  }

  protected override defaultPageSize(): number {
    return 1000;
  }

  override async create(earning: Earning): Promise<Earning> {
    const created = await this.delegate.create({
      data: EarningPrismaMapper.toEarningCreatePersistence(earning),
      include: EARNING_RELATIONS,
    });
    return EarningPrismaMapper.toEarningDomain(created)!;
  }

  override async update(id: string, earning: Earning): Promise<Earning> {
    const updated = await this.delegate.update({
      where: { id },
      data: EarningPrismaMapper.toEarningUpdatePersistence(earning),
      include: EARNING_RELATIONS,
    });
    return EarningPrismaMapper.toEarningDomain(updated)!;
  }

  override async delete(id: string): Promise<void> {
    await this.delegate.update({
      where: { id },
      data: { deletedAt: new Date() },
    });
  }
}
