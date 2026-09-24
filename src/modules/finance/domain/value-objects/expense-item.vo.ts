import { BusinessException } from '@ssdev-toolkit/nestjs-core';

export class ExpenseItem {
  constructor(
    public itemName: string,
    public description: string | undefined,
    public amount: number,
  ) {
    if (!itemName || itemName.trim().length === 0) {
      throw new BusinessException('Expense item name is required');
    }
    if (amount <= 0) {
      throw new BusinessException('Expense item amount must be positive');
    }
  }
}
