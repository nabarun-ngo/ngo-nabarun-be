import { AccountDetailFilterDto } from '../../../presentation/dtos/account.dto';

export class ListAccountsQuery {
  constructor(
    public readonly filter: AccountDetailFilterDto={},
    public readonly userId?: string,
  ) {}
}

