import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { Prisma, PrismaClient } from '../../prisma/client';
import type {
  BeneficiaryWhereInput,
  BeneficiaryWhereUniqueInput,
  BeneficiaryUncheckedCreateInput,
  BeneficiaryUncheckedUpdateInput,
  BeneficiaryOrderByWithRelationInput,
} from '../../prisma/models/Beneficiary';
import { IBeneficiaryRepository } from '../../../../modules/project/domain/repositories/beneficiary.repository';
import { Beneficiary, BeneficiaryFilter } from '../../../../modules/project/domain/aggregates/beneficiary/beneficiary.aggregate';
import { BeneficiaryPrismaMapper } from '../mapper/beneficiary-prisma.mapper';

type BeneficiaryRow = Prisma.BeneficiaryGetPayload<object>;

@Injectable()
export class BeneficiaryPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'beneficiary',
    Beneficiary,
    string,
    BeneficiaryFilter,
    BeneficiaryRow,
    BeneficiaryWhereInput,
    BeneficiaryWhereUniqueInput,
    BeneficiaryUncheckedCreateInput,
    BeneficiaryUncheckedUpdateInput,
    BeneficiaryOrderByWithRelationInput
  >
  implements IBeneficiaryRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'beneficiary');
  }

  protected toDomain(row: BeneficiaryRow): Beneficiary {
    return BeneficiaryPrismaMapper.toDomain(row)!;
  }

  protected toCreateInput(entity: Beneficiary): BeneficiaryUncheckedCreateInput {
    return BeneficiaryPrismaMapper.toCreate(entity);
  }

  protected toUpdateInput(_id: string, entity: Beneficiary): BeneficiaryUncheckedUpdateInput {
    return BeneficiaryPrismaMapper.toUpdate(entity);
  }

  protected toUniqueWhere(id: string): BeneficiaryWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(props?: BeneficiaryFilter): BeneficiaryWhereInput {
    return {
      deletedAt: null,
      ...(props?.projectId ? { projectId: props.projectId } : {}),
      ...(props?.status ? { status: props.status } : {}),
      ...(props?.type ? { type: props.type } : {}),
      ...(props?.category ? { category: props.category } : {}),
    };
  }

  protected override defaultOrderBy(): BeneficiaryOrderByWithRelationInput {
    return { enrollmentDate: 'desc' };
  }

  protected override defaultPageSize(): number {
    return 20;
  }

  async countByProject(projectId: string): Promise<number> {
    return this.count({ projectId });
  }

  async countForProjects(projectIds: string[], filter?: BeneficiaryFilter): Promise<number> {
    if (projectIds.length === 0) {
      return 0;
    }
    return this.delegate.count({
      where: {
        deletedAt: null,
        projectId: { in: projectIds },
        ...(filter?.status ? { status: filter.status } : {}),
        ...(filter?.type ? { type: filter.type } : {}),
        ...(filter?.category ? { category: filter.category } : {}),
      },
    });
  }

  override async delete(id: string): Promise<void> {
    await this.delegate.update({ where: { id }, data: { deletedAt: new Date() } });
  }
}
