import { IRepository } from '@ssdev-toolkit/nestjs-core';
import { Beneficiary, BeneficiaryFilter } from '../aggregates/beneficiary/beneficiary.aggregate';
export const IBeneficiaryRepository = Symbol('IBeneficiaryRepository');
export interface IBeneficiaryRepository extends IRepository<Beneficiary, string, BeneficiaryFilter> {
  countByProject(projectId: string): Promise<number>;
  countForProjects(projectIds: string[], filter?: BeneficiaryFilter): Promise<number>;
}
