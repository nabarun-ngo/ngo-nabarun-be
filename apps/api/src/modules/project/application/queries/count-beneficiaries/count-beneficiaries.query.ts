import { BeneficiaryDetailFilterDto } from '../../dtos/beneficiary.dto';
import { ProjectDetailFilterDto } from '../../dtos/project.dto';

export class CountBeneficiariesQuery {
  constructor(
    public readonly projectFilter: ProjectDetailFilterDto = {},
    public readonly beneficiaryFilter: BeneficiaryDetailFilterDto = {},
  ) {}
}
