import { TransactionDetailFilterDto } from '../../../presentation/dtos/transaction.dto';

export class ListAccountTransactionsQuery {
  constructor(
    public readonly accountId: string,
    public readonly filter: TransactionDetailFilterDto={},
    public readonly userId?: string,
  ) {}
}

