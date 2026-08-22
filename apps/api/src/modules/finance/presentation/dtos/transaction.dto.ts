import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsOptional, IsNumber, IsEnum, IsDate, Min } from 'class-validator';
import { Transform, Type } from 'class-transformer';
import { AccountDetailDto } from './account.dto';
import { TransactionRefType, TransactionStatus, TransactionType } from '../../domain/enums/transaction.enum';

/**
 * Transaction Detail DTO - matches legacy TransactionDetail
 */
export class TransactionDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  @IsString()
  txnId: string;

  @ApiPropertyOptional({ example: 'TXN-2026-000482' })
  @IsOptional()
  @IsString()
  txnNumber?: string;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  @IsDate()
  @Type(() => Date)
  txnDate: Date;

  @ApiProperty({ example: 1500 })
  @IsNumber()
  @Min(0.01)
  txnAmount: number;

  @ApiProperty({ enum: TransactionType, example: TransactionType.IN })
  @IsEnum(TransactionType)
  txnType: TransactionType;

  @ApiProperty({ enum: TransactionStatus, example: TransactionStatus.SUCCESS })
  @IsEnum(TransactionStatus)
  txnStatus: TransactionStatus;

  @ApiProperty({ example: 'Monthly donation received' })
  @IsString()
  txnDescription: string;

  @ApiPropertyOptional({ example: 'Donation from Asha Verma' })
  @IsOptional()
  @IsString()
  txnParticulars?: string;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsOptional()
  @IsString()
  txnRefId?: string;

  @ApiPropertyOptional({ enum: TransactionRefType, example: TransactionRefType.DONATION })
  @IsOptional()
  @IsEnum(TransactionRefType)
  txnRefType?: TransactionRefType;

  @ApiPropertyOptional({ example: 25000 })
  @IsOptional()
  @IsNumber()
  accBalance?: number;

  @ApiPropertyOptional({ example: 'CREDIT' })
  @IsOptional()
  @IsString()
  accTxnType?: string;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsOptional()
  @IsString()
  transferFrom?: string;

  @ApiPropertyOptional({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  @IsOptional()
  @IsString()
  transferTo?: string;

  @ApiProperty({ example: 'TXN-2026-000482' })
  @IsString()
  transactionRef: string;

  @ApiPropertyOptional()
  @IsOptional()
  @Type(() => AccountDetailDto)
  account?: AccountDetailDto;
}

/**
 * Transaction Detail Filter DTO
 */
export class TransactionDetailFilterDto {
  @ApiPropertyOptional({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  @IsOptional()
  @IsString()
  txnId?: string;

  @ApiPropertyOptional({ enum: TransactionType, isArray: true, example: [TransactionType.IN, TransactionType.OUT] })
  @IsOptional()
  @IsEnum(TransactionType, { each: true })
  @Transform(({ value }) =>
    Array.isArray(value) ? value : value ? [value] : undefined
  )
  txnType?: TransactionType[];

  @ApiPropertyOptional({ enum: TransactionStatus, isArray: true, example: [TransactionStatus.SUCCESS] })
  @IsOptional()
  @IsEnum(TransactionStatus, { each: true })
  @Transform(({ value }) =>
    Array.isArray(value) ? value : value ? [value] : undefined
  )
  txnStatus?: TransactionStatus[];

  @ApiPropertyOptional({ example: 'TXN-2026-000482' })
  @IsOptional()
  @IsString()
  transactionRef?: string;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  startDate?: Date;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-31T23:59:59.000Z' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  endDate?: Date;
}

/**
 * Create Transaction DTO
 */
export class CreateTransactionDto {
  @ApiProperty({ enum: TransactionType, example: TransactionType.IN })
  @IsEnum(TransactionType)
  txnType: TransactionType;

  @ApiProperty({ minimum: 0.01, example: 1500 })
  @IsNumber()
  @Min(0.01)
  txnAmount: number;

  @ApiProperty({ example: 'INR' })
  @IsString()
  currency: string;

  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  @IsString()
  accountId: string;

  @ApiProperty({ example: 'Monthly donation received' })
  @IsString()
  txnDescription: string;

  @ApiPropertyOptional({ example: 'Donation from Asha Verma' })
  @IsOptional()
  @IsString()
  txnParticulars?: string;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsOptional()
  @IsString()
  txnRefId?: string;

  @ApiPropertyOptional({ enum: TransactionRefType, example: TransactionRefType.DONATION })
  @IsOptional()
  @IsEnum(TransactionRefType)
  txnRefType?: TransactionRefType;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  txnDate?: Date;
}


export class ReverseTransactionDto {
  @ApiProperty({ example: 'TXN-2026-000482' })
  @IsString()
  transactionRef: string;

  @ApiProperty({ example: 'Reversed duplicate donation entry' })
  @IsString()
  comment: string;
}
