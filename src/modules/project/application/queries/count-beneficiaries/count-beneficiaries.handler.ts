import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IBeneficiaryRepository } from '../../../domain/repositories/beneficiary.repository';
import { IProjectRepository } from '../../../domain/repositories/project.repository';
import { CountBeneficiariesQuery } from './count-beneficiaries.query';

@QueryHandler(CountBeneficiariesQuery)
@Injectable()
export class CountBeneficiariesHandler implements IQueryHandler<CountBeneficiariesQuery, number> {
  constructor(
    @Inject(IProjectRepository) private readonly projectRepo: IProjectRepository,
    @Inject(IBeneficiaryRepository) private readonly beneficiaryRepo: IBeneficiaryRepository,
  ) {}

  async execute(query: CountBeneficiariesQuery): Promise<number> {
    const projects = await this.projectRepo.findAll(query.projectFilter);
    const projectIds = projects.map((project) => project.id);

    return this.beneficiaryRepo.countForProjects(projectIds, query.beneficiaryFilter);
  }
}
