import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BaseFilter, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { IBeneficiaryRepository } from '../../../domain/repositories/beneficiary.repository';
import { BeneficiaryMapper } from '../../mappers/beneficiary.mapper';
import { BeneficiaryDetailDto } from '../../dtos/beneficiary.dto';
import { ListBeneficiariesQuery } from './list-beneficiaries.query';

@QueryHandler(ListBeneficiariesQuery)
@Injectable()
export class ListBeneficiariesHandler implements IQueryHandler<ListBeneficiariesQuery, PagedResponse<BeneficiaryDetailDto>> {
  constructor(@Inject(IBeneficiaryRepository) private readonly repo: IBeneficiaryRepository) { }

  async execute(q: ListBeneficiariesQuery): Promise<PagedResponse<BeneficiaryDetailDto>> {
    const { pageIndex, pageSize, sortBy, sortDir, ...props } = q.filter ?? {};
    const page = await this.repo.findPaged(
      new BaseFilter({ ...props, projectId: q.projectId }, pageIndex ?? 0, pageSize ?? 20, sortBy, sortDir),
    );
    return {
      content: page.content.map(BeneficiaryMapper.toDto),
      totalSize: page.totalSize,
      pageIndex: page.pageIndex,
      pageSize: page.pageSize,
    };
  }
}
