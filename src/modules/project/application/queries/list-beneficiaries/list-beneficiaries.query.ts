import { BeneficiaryDetailFilterDto } from '../../dtos/beneficiary.dto';

export class ListBeneficiariesQuery {
  constructor(
    public readonly projectId: string,
    public readonly filter: BeneficiaryDetailFilterDto = {},
  ) {}
}
