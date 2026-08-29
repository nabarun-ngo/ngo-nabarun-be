import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { Prisma, PrismaClient } from '../../prisma/client';
import type {
  ReportWhereInput,
  ReportWhereUniqueInput,
  ReportCreateInput,
  ReportUpdateInput,
  ReportOrderByWithRelationInput,
} from '../../prisma/models/Report';
import { Report, ReportFilter } from '../../../../modules/reporting/domain/aggregates/report/report.aggregate';
import { IReportRepository } from '../../../../modules/reporting/domain/repositories/report.repository';
import { ReportPrismaMapper } from '../mappers/report-prisma.mapper';

const includeUsers = {
  requestedBy: { select: { id: true, firstName: true, lastName: true } },
  approvedBy: { select: { id: true, firstName: true, lastName: true } },
} as const;

export type ReportPersistence = Prisma.ReportGetPayload<{
  include: {
    requestedBy: { select: { id: true; firstName: true; lastName: true } };
    approvedBy: { select: { id: true; firstName: true; lastName: true } };
  };
}>;

@Injectable()
export class ReportPrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'report',
    Report,
    string,
    ReportFilter,
    ReportPersistence,
    ReportWhereInput,
    ReportWhereUniqueInput,
    ReportCreateInput,
    ReportUpdateInput,
    ReportOrderByWithRelationInput,
    typeof includeUsers
  >
  implements IReportRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'report');
  }

  protected toDomain(row: ReportPersistence): Report {
    return ReportPrismaMapper.toDomain(row)!;
  }

  protected toCreateInput(entity: Report): ReportCreateInput {
    return ReportPrismaMapper.toCreatePersistence(entity);
  }

  protected toUpdateInput(_id: string, entity: Report): ReportUpdateInput {
    return ReportPrismaMapper.toUpdatePersistence(entity);
  }

  protected toUniqueWhere(id: string): ReportWhereUniqueInput {
    return { id };
  }

  protected toFilterWhere(filter?: ReportFilter): ReportWhereInput {
    return {
      ...(filter?.reportCode ? { reportCode: filter.reportCode } : {}),
      ...(filter?.status?.length ? { status: { in: filter.status } } : {}),
      ...(filter?.requestedById ? { requestedById: filter.requestedById } : {}),
    };
  }

  protected override defaultOrderBy(): ReportOrderByWithRelationInput {
    return { createdAt: 'desc' };
  }

  protected override toInclude(): typeof includeUsers {
    return includeUsers;
  }

  protected override defaultPageSize(): number {
    return 20;
  }

  override async create(entity: Report): Promise<Report> {
    const created = await this.delegate.create({
      data: ReportPrismaMapper.toCreatePersistence(entity),
      include: includeUsers,
    });
    return ReportPrismaMapper.toDomain(created)!;
  }

  override async update(id: string, entity: Report): Promise<Report> {
    const updated = await this.delegate.update({
      where: { id },
      data: ReportPrismaMapper.toUpdatePersistence(entity),
      include: includeUsers,
    });
    return ReportPrismaMapper.toDomain(updated)!;
  }
}
