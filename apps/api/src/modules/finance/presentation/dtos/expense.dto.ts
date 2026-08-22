import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsOptional, IsNumber, IsEnum, IsArray, ValidateNested, IsDate, Min } from 'class-validator';
import { Transform, Type } from 'class-transformer';
import { ExpenseRefType, ExpenseStatus } from '../../domain/enums/expense.enum';
import { FinanceUserDto } from '../../application/dtos/finance-user.dto';
import { KeyValueOption } from '../../application/ports/finance-reference-data.port';


/**
 * Expense Item Detail DTO
 */
export class ExpenseItemDetailDto {
  @ApiProperty({ example: 'Venue rent' })
  @IsString()
  itemName: string;

  @ApiProperty({ example: 4500 })
  @IsNumber()
  @Min(1)
  amount: number;
}

/**
 * Expense Detail DTO - matches legacy ExpenseDetail
 */
export class ExpenseDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  id: string;

  @ApiProperty({ example: 'Literacy drive venue expense' })
  name: string;

  @ApiProperty({ example: 'Venue rent for literacy drive' })
  description: string;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  expenseDate: Date;

  @ApiPropertyOptional()
  createdBy?: FinanceUserDto; // UserDetail reference

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  createdOn: Date;

  @ApiPropertyOptional({ example: true })
  isDeligated?: boolean;

  @ApiPropertyOptional()
  paidBy?: FinanceUserDto; // UserDetail reference

  @ApiPropertyOptional()
  finalizedBy?: FinanceUserDto; // UserDetail reference

  @ApiProperty({ enum: ExpenseStatus, example: ExpenseStatus.SETTLED })
  status: ExpenseStatus;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-16T11:45:00.000Z' })
  finalizedOn?: Date;

  @ApiPropertyOptional()
  settledBy?: FinanceUserDto; // UserDetail reference

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-18T14:20:00.000Z' })
  settledOn?: Date;

  @ApiPropertyOptional({ type: () => [ExpenseItemDetailDto] })
  expenseItems?: ExpenseItemDetailDto[];

  @ApiProperty({ example: 4500 })
  finalAmount: number;

  @ApiPropertyOptional({ enum: ExpenseRefType, example: ExpenseRefType.EVENT })
  expenseRefType?: ExpenseRefType;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  expenseRefId?: string;

  @ApiPropertyOptional({ example: 'TXN-2026-000482' })
  txnNumber?: string;

  @ApiPropertyOptional({ example: 'Literacy Drive 2026' })
  activityName?: string;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  activityId?: string;

  @ApiPropertyOptional({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  settlementAccountId?: string;

  @ApiPropertyOptional()
  sendBackBy?: FinanceUserDto; // UserDetail reference

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-17T10:15:00.000Z' })
  sendBackOn?: Date;

  @ApiPropertyOptional({ example: 'Settled via bank transfer' })
  remarks?: string;
}

/**
 * Expense Detail Filter DTO
 */
export class ExpenseDetailFilterDto {
  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  @IsOptional()
  startDate?: Date;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-31T23:59:59.000Z' })
  @IsOptional()
  endDate?: Date;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsOptional()
  @IsString()
  expenseRefId?: string;

  @ApiPropertyOptional({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  @IsOptional()
  @IsString()
  expenseId?: string;

  @ApiPropertyOptional({ enum: ExpenseStatus, isArray: true, example: [ExpenseStatus.SETTLED, ExpenseStatus.FINALIZED] })
  @IsOptional()
  @IsArray()
  @IsEnum(ExpenseStatus, { each: true })
  @Transform(({ value }) =>
    Array.isArray(value) ? value : value ? [value] : undefined
  )
  expenseStatus?: ExpenseStatus[];

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @IsOptional()
  @IsString()
  payerId?: string;
}

/**
 * Create Expense DTO
 */
export class CreateExpenseDto {
  @ApiProperty({ example: 'Literacy drive venue expense' })
  @IsString()
  name: string;

  @ApiPropertyOptional({ example: 'Venue rent for literacy drive' })
  @IsString()
  @IsOptional()
  description?: string;

  @ApiPropertyOptional({ example: 'INR' })
  @IsOptional()
  @IsString()
  currency?: string;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  expenseDate?: Date;

  @ApiProperty({ enum: ExpenseRefType, example: ExpenseRefType.EVENT })
  @IsEnum(ExpenseRefType)
  expenseRefType: ExpenseRefType;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsOptional()
  @IsString()
  expenseRefId?: string;

  @ApiPropertyOptional({ type: () => [ExpenseItemDetailDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => ExpenseItemDetailDto)
  expenseItems?: ExpenseItemDetailDto[];

  @ApiProperty({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @IsString()
  payerId: string;
}

/**
 * Update Expense DTO
 */
export class UpdateExpenseDto {
  @ApiPropertyOptional({ example: 'Literacy drive venue expense' })
  @IsOptional()
  @IsString()
  name?: string;

  @ApiPropertyOptional({ example: ExpenseRefType.EVENT })
  @IsOptional()
  @IsEnum(ExpenseRefType)
  expenseRefType?: ExpenseRefType;


  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsOptional()
  @IsString()
  expenseRefId?: string;

  @ApiPropertyOptional({ example: 'Venue rent for literacy drive' })
  @IsOptional()
  @IsString()
  description?: string;

  @ApiPropertyOptional({ example: ExpenseStatus.SETTLED })
  @IsOptional()
  @IsEnum(ExpenseStatus)
  status?: ExpenseStatus;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  expenseDate?: Date;

  @ApiPropertyOptional({ type: () => [ExpenseItemDetailDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => ExpenseItemDetailDto)
  expenseItems?: ExpenseItemDetailDto[];

  @ApiPropertyOptional({ example: 'Settled via bank transfer' })
  @IsOptional()
  @IsString()
  remarks?: string;

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @IsOptional()
  @IsString()
  payerId?: string;
}

export class ExpenseStatusGroupsDto {
  @ApiProperty({ type: [String], example: ['DRAFT', 'SUBMITTED', 'FINALIZED', 'SEND_BACK'] })
  outstanding: string[];

  @ApiProperty({ type: [String], example: ['SETTLED'] })
  closed: string[];

  @ApiProperty({ type: [String], example: [] })
  excluded: string[];
}

export class ExpenseRefDataDto {
  @ApiProperty({ example: [{ key: 'SETTLED', value: 'Settled' }, { key: 'FINALIZED', value: 'Finalized' }] })
  expenseStatuses?: KeyValueOption[];

  @ApiProperty({ example: [{ key: 'EVENT', value: 'Event' }, { key: 'OPERATIONAL', value: 'Operational' }] })
  expenseRefTypes?: KeyValueOption[];

  @ApiPropertyOptional({ type: () => ExpenseStatusGroupsDto })
  expenseStatusGroups?: ExpenseStatusGroupsDto;
}

