import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import {
  IsDateString,
  IsEnum,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsString,
  Min,
  ValidateIf,
} from 'class-validator';
import { AssetCategory, AssetStatus } from '../../domain/enums/asset.enum';

export class AssetCustodyRecordDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' }) id!: string;
  @ApiProperty({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) custodianUserId!: string;
  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) assignedAt!: Date;
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) assignedById?: string;
  @ApiPropertyOptional({ example: '2026-04-01T09:30:00.000Z' }) returnedAt?: Date;
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) returnedById?: string;
  @ApiPropertyOptional({ example: 'Issued for field camp' }) notes?: string;
}

export class CreateAssetDto {
  @ApiProperty({ example: 'Dell Latitude laptop' }) @IsNotEmpty() @IsString() name!: string;
  @ApiProperty({ enum: AssetCategory, example: AssetCategory.ELECTRONICS })
  @IsNotEmpty()
  @IsEnum(AssetCategory)
  category!: AssetCategory;

  @ApiPropertyOptional({ example: 'SN-DELL-88421' }) @IsOptional() @IsString() serialNumber?: string;
  @ApiPropertyOptional({ example: 'Nabarun office — room 2' }) @IsOptional() @IsString() location?: string;
  @ApiPropertyOptional({ enum: AssetStatus, example: AssetStatus.AVAILABLE })
  @IsOptional()
  @IsEnum(AssetStatus)
  status?: AssetStatus;

  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsOptional()
  @IsString()
  projectId?: string;

  @ApiPropertyOptional({ example: '9a1b2c3d-4e5f-6789-abcd-ef0123456789' })
  @IsOptional()
  @IsString()
  expenseId?: string;

  @ApiPropertyOptional({ example: '2026-01-15' }) @IsOptional() @IsDateString() purchaseDate?: string;
  @ApiPropertyOptional({ example: 45000 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  purchaseCost?: number;

  @ApiPropertyOptional({ example: 40000 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  currentValue?: number;

  @ApiPropertyOptional({ example: 'INR' })
  @ValidateIf((o: CreateAssetDto) => o.purchaseCost != null || o.currentValue != null)
  @IsNotEmpty()
  @IsString()
  currency?: string;

  @ApiPropertyOptional({ example: 'Straight-line over 3 years; book value updated annually' })
  @IsOptional()
  @IsString()
  depreciationMethodNotes?: string;

  @ApiPropertyOptional({ example: 'Battery health checked March 2026' })
  @IsOptional()
  @IsString()
  maintenanceNotes?: string;
}

export class UpdateAssetDto {
  @ApiPropertyOptional({ example: 'Dell Latitude laptop' }) @IsOptional() @IsString() name?: string;
  @ApiPropertyOptional({ enum: AssetCategory }) @IsOptional() @IsEnum(AssetCategory) category?: AssetCategory;
  @ApiPropertyOptional({ example: 'SN-DELL-88421' }) @IsOptional() @IsString() serialNumber?: string;
  @ApiPropertyOptional({ example: 'Nabarun office — room 2' }) @IsOptional() @IsString() location?: string;
  @ApiPropertyOptional({ enum: AssetStatus }) @IsOptional() @IsEnum(AssetStatus) status?: AssetStatus;
  @ApiPropertyOptional() @IsOptional() @IsString() projectId?: string;
  @ApiPropertyOptional() @IsOptional() @IsString() expenseId?: string;
  @ApiPropertyOptional({ example: '2026-01-15' }) @IsOptional() @IsDateString() purchaseDate?: string;
  @ApiPropertyOptional({ example: 45000 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  purchaseCost?: number;
  @ApiPropertyOptional({ example: 40000 })
  @IsOptional()
  @Type(() => Number)
  @IsNumber()
  @Min(0)
  currentValue?: number;
  @ApiPropertyOptional({ example: 'INR' }) @IsOptional() @IsString() currency?: string;
  @ApiPropertyOptional() @IsOptional() @IsString() depreciationMethodNotes?: string;
  @ApiPropertyOptional() @IsOptional() @IsString() maintenanceNotes?: string;
}

export class AssignAssetCustodyDto {
  @ApiProperty({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @IsNotEmpty()
  @IsString()
  custodianUserId!: string;

  @ApiPropertyOptional({ example: 'Issued for field camp' }) @IsOptional() @IsString() notes?: string;
}

export class ReturnAssetCustodyDto {
  @ApiPropertyOptional({ example: 'Returned with charger' }) @IsOptional() @IsString() notes?: string;
}

export class AssetDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' }) id!: string;
  @ApiProperty({ example: 'Dell Latitude laptop' }) name!: string;
  @ApiProperty({ enum: AssetCategory }) category!: AssetCategory;
  @ApiPropertyOptional({ example: 'SN-DELL-88421' }) serialNumber?: string;
  @ApiPropertyOptional({ example: 'Nabarun office — room 2' }) location?: string;
  @ApiProperty({ enum: AssetStatus }) status!: AssetStatus;
  @ApiPropertyOptional() custodianUserId?: string;
  @ApiPropertyOptional() projectId?: string;
  @ApiPropertyOptional() expenseId?: string;
  @ApiPropertyOptional() purchaseDate?: Date;
  @ApiPropertyOptional({ example: 45000 }) purchaseCost?: number;
  @ApiPropertyOptional({ example: 'INR' }) currency?: string;
  @ApiPropertyOptional({ example: 40000 }) currentValue?: number;
  @ApiPropertyOptional() depreciationMethodNotes?: string;
  @ApiPropertyOptional() maintenanceNotes?: string;
  @ApiPropertyOptional() createdById?: string;
  @ApiPropertyOptional() updatedById?: string;
  @ApiPropertyOptional({ type: [AssetCustodyRecordDto] }) custodyHistory?: AssetCustodyRecordDto[];
  @ApiProperty() createdAt!: Date;
  @ApiProperty() updatedAt!: Date;
}

export class AssetDetailFilterDto {
  @ApiPropertyOptional({ enum: AssetStatus }) @IsOptional() @IsEnum(AssetStatus) status?: AssetStatus;
  @ApiPropertyOptional({ enum: AssetCategory }) @IsOptional() @IsEnum(AssetCategory) category?: AssetCategory;
  @ApiPropertyOptional() @IsOptional() @IsString() custodianUserId?: string;
  @ApiPropertyOptional() @IsOptional() @IsString() projectId?: string;
}

export class AssetListResponseDto {
  @ApiProperty({ type: [AssetDetailDto] }) items!: AssetDetailDto[];
  @ApiProperty({ example: 42 }) total!: number;
  @ApiProperty({ example: 0 }) pageIndex!: number;
  @ApiProperty({ example: 20 }) pageSize!: number;
}
