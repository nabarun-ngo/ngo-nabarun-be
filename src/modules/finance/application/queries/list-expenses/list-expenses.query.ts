import { ExpenseStatus } from '../../../domain/enums/expense.enum';
import { ExpenseDetailFilterDto } from '../../../presentation/dtos/expense.dto';

export class ListExpensesQuery {
  constructor(
    public readonly filter: ExpenseDetailFilterDto = {},
  ) {}
}

