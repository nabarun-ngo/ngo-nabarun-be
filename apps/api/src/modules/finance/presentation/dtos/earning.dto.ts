import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsOptional, IsNumber, IsEnum, IsDate, Min } from 'class-validator';
import { Transform, Type } from 'class-transformer';
import { EarningCategory, EarningStatus } from '../../domain/enums/earning.enum';
import { KeyValueOption } from '../../application/ports/finance-reference-data.port';

/**
 * Earning Detail DTO
 */
export class EarningDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  id: string;

  @ApiProperty({ enum: EarningCategory, example: EarningCategory.INTEREST })
  category: EarningCategory;

  @ApiProperty({ example: 499.5 })
  amount: number;

  @ApiProperty({ example: 'INR' })
  currency: string;

  @ApiProperty({ enum: EarningStatus, example: EarningStatus.RECEIVED })
  status: EarningStatus;

  @ApiProperty({ example: 'Quarterly savings account interest' })
  description: string;

  @ApiProperty({ example: 'HDFC Bank' })
  source: string;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  referenceId?: string;

  @ApiPropertyOptional({ example: 'ACCOUNT' })
  referenceType?: string;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  accountId?: string;

  @ApiPropertyOptional({ example: 'TXN-2026-000482' })
  transactionId?: string;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  earningDate?: Date;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T11:45:00.000Z' })
  receivedDate?: Date;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  createdAt?: Date;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T11:45:00.000Z' })
  updatedAt?: Date;

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @IsOptional()
  @IsString()
  receivedBy?: string;

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @IsOptional()
  @IsString()
  createdBy?: string;
}

/**
 * Earning Detail Filter DTO
 */
export class EarningDetailFilterDto {
  @ApiPropertyOptional({ enum: EarningStatus, isArray: true, example: [EarningStatus.RECEIVED, EarningStatus.PENDING] })
  @IsOptional()
  @Transform(({ value }) =>
    Array.isArray(value) ? value : value ? [value] : undefined
  )
  status?: EarningStatus[];

  @ApiPropertyOptional({ enum: EarningCategory, isArray: true, example: [EarningCategory.INTEREST] })
  @IsOptional()
  @Transform(({ value }) =>
    Array.isArray(value) ? value : value ? [value] : undefined
  )
  category?: EarningCategory[];

  @ApiPropertyOptional({ example: 'HDFC Bank' })
  @IsOptional()
  @IsString()
  source?: string;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsOptional()
  @IsString()
  referenceId?: string;

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
 * Create Earning DTO
 */
export class CreateEarningDto {
  @ApiProperty({ enum: EarningCategory, example: EarningCategory.INTEREST })
  @IsEnum(EarningCategory)
  category: EarningCategory;

  @ApiProperty({ minimum: 0.01, example: 499.5 })
  @IsNumber()
  @Min(0.01)
  amount: number;

  @ApiProperty({ example: 'INR' })
  @IsString()
  currency: string;

  @ApiProperty({ example: 'HDFC Bank' })
  @IsOptional()
  @IsString()
  source: string;

  @ApiPropertyOptional({ example: 'Quarterly savings account interest' })
  @IsString()
  description?: string;

  @ApiPropertyOptional({
    example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68',
    description: 'Active bank or investment account receiving an interest earning',
  })
  @IsOptional()
  @IsString()
  accountId?: string;

}

/**
 * Update Earning DTO
 */
export class UpdateEarningDto {
  @ApiPropertyOptional({ enum: EarningCategory, example: EarningCategory.INTEREST })
  @IsOptional()
  @IsEnum(EarningCategory)
  category?: EarningCategory;

  @ApiPropertyOptional({ example: 'Quarterly savings account interest' })
  @IsOptional()
  @IsString()
  description?: string;

  @ApiPropertyOptional({ example: 'HDFC Bank' })
  @IsOptional()
  @IsString()
  source?: string;

  @ApiPropertyOptional({ minimum: 0.01, example: 499.5 })
  @IsOptional()
  @IsNumber()
  @Min(0.01)
  amount?: number;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  earningDate?: Date;

  @ApiPropertyOptional({ enum: EarningStatus, example: EarningStatus.RECEIVED })
  @IsOptional()
  @IsEnum(EarningStatus)
  status?: EarningStatus;


  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsOptional()
  @IsString()
  accountId?: string;
}

export class EarningStatusGroupsDto {
  @ApiProperty({ type: [String], example: ['PENDING'] })
  outstanding: string[];

  @ApiProperty({ type: [String], example: ['RECEIVED', 'CANCELLED'] })
  closed: string[];

  @ApiProperty({ type: [String], example: [] })
  excluded: string[];
}

export class EarningRefDataDto {
  @ApiProperty({ example: [{ key: 'RECEIVED', value: 'Received' }, { key: 'PENDING', value: 'Pending' }] })
  earningStatuses?: KeyValueOption[];

  @ApiProperty({ example: [{ key: 'INTEREST', value: 'Interest' }, { key: 'GRANT', value: 'Grant' }] })
  earningCategories?: KeyValueOption[];

  @ApiPropertyOptional({ type: () => EarningStatusGroupsDto })
  earningStatusGroups?: EarningStatusGroupsDto;
}