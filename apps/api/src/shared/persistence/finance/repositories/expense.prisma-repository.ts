import { Injectable } from '@nestjs/common';
import { BasePrismaService, PrismaCrudRepositoryBase } from '@nabarun-ngo/nestjs-shared-persistence';
import { Prisma, PrismaClient } from '../../prisma/client';
import type {
  ExpenseWhereInput,
  ExpenseWhereUniqueInput,
  ExpenseUncheckedCreateInput,
  ExpenseUncheckedUpdateInput,
  ExpenseOrderByWithRelationInput,
} from '../../prisma/models/Expense';
import { IExpenseRepository, ExpenseFilter } from '../../../../modules/finance/domain/repositories/expense.repository';
import { Expense } from '../../../../modules/finance/domain/aggregates/expense/expense.aggregate';
import { ExpenseStatus } from '../../../../modules/finance/domain/enums/expense.enum';
import { ExpensePrismaMapper } from '../mapper/expense-prisma.mapper';


export type ExpensePersistence = Prisma.ExpenseGetPayload<{
  include: {
    account: true;
    createdBy: true;
    updatedBy: true;
    finalizedBy: true;
    settledBy: true;
    rejectedBy: true;
    submittedBy: true;
    paidBy: true;
    activity: true;
  }
}>;

const EXPENSE_RELATIONS = {
  account: true,
  createdBy: true,
  updatedBy: true,
  finalizedBy: true,
  settledBy: true,
  rejectedBy: true,
  submittedBy: true,
  paidBy: true,
  activity: true,
} as const;

@Injectable()
export class ExpensePrismaRepository
  extends PrismaCrudRepositoryBase<
    PrismaClient,
    'expense',
    Expense,
    string,
    ExpenseFilter,
    ExpensePersistence,
    ExpenseWhereInput,
    ExpenseWhereUniqueInput,
    ExpenseUncheckedCreateInput,
    ExpenseUncheckedUpdateInput,
    ExpenseOrderByWithRelationInput,
    typeof EXPENSE_RELATIONS
  >
  implements IExpenseRepository {
  constructor(database: BasePrismaService<PrismaClient>) {
    super(database, 'expense');
  }

  protected toFilterWhere(props?: ExpenseFilter): ExpenseWhereInput {
    const where: ExpenseWhereInput = {
      ...(props?.expenseStatus ? { status: { in: props.expenseStatus } } : {}),
      ...(props?.expenseId ? { id: props.expenseId } : {}),
      ...(props?.payerId ? { paidById: props.payerId } : {}),
      ...(props?.expenseRefId ? { referenceId: props.expenseRefId } : {}),
      ...(props?.startDate || props?.endDate
        ? {
          expenseDate: {
            ...(props.startDate ? { gte: props.startDate } : {}),
            ...(props.endDate ? { lte: props.endDate } : {}),
          },
        }
        : {}),
      deletedAt: null,
    };
    return where;
  }

  async findById(id: string): Promise<Expense | null> {
    const expense = await this.delegate.findUnique({
      where: { id },
      include: EXPENSE_RELATIONS,
    });

    return ExpensePrismaMapper.toExpenseDomain(expense!);
  }


  async findByStatus(status: ExpenseStatus): Promise<Expense[]> {
    const expenses = await this.delegate.findMany({
      where: { status, deletedAt: null },
      orderBy: { expenseDate: 'desc' },
      include: EXPENSE_RELATIONS,
    });

    return expenses.map(m => ExpensePrismaMapper.toExpenseDomain(m)!);
  }

  async findByRequestedBy(userId: string): Promise<Expense[]> {
    const expenses = await this.delegate.findMany({
      where: { createdById: userId, deletedAt: null },
      orderBy: { expenseDate: 'desc' },
      include: EXPENSE_RELATIONS,
    });

    return expenses.map(m => ExpensePrismaMapper.toExpenseDomain(m)!);
  }



  async create(expense: Expense): Promise<Expense> {
    const createData: Prisma.ExpenseUncheckedCreateInput = {
      ...ExpensePrismaMapper.toExpenseCreatePersistence(expense),
    };

    const created = await this.delegate.create({
      data: createData,
      include: EXPENSE_RELATIONS,
    });

    return ExpensePrismaMapper.toExpenseDomain(created)!;
  }

  async update(id: string, expense: Expense): Promise<Expense> {
    const updateData: Prisma.ExpenseUncheckedUpdateInput = {
      ...ExpensePrismaMapper.toExpenseUpdatePersistence(expense),
    };

    const updated = await this.delegate.update({
      where: { id },
      data: updateData,
      include: EXPENSE_RELATIONS,
    });

    return ExpensePrismaMapper.toExpenseDomain(updated)!;
  }

  protected toDomain(row: ExpensePersistence): Expense {
    return ExpensePrismaMapper.toExpenseDomain(row)!;
  }

  protected toCreateInput(expense: Expense): ExpenseUncheckedCreateInput {
    return ExpensePrismaMapper.toExpenseCreatePersistence(expense);
  }

  protected toUpdateInput(_id: string, expense: Expense): ExpenseUncheckedUpdateInput {
    return ExpensePrismaMapper.toExpenseUpdatePersistence(expense);
  }

  protected toUniqueWhere(id: string): ExpenseWhereUniqueInput {
    return { id };
  }

  protected override supportsSoftDelete(): boolean {
    return true;
  }

  protected override defaultOrderBy(): ExpenseOrderByWithRelationInput {
    return { expenseDate: 'desc' };
  }

  protected override toInclude(): typeof EXPENSE_RELATIONS {
    return EXPENSE_RELATIONS;
  }
}

