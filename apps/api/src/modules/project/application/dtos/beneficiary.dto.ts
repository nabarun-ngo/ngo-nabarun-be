import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsDate, IsEnum, IsNumber, IsOptional, IsString, Min } from 'class-validator';
import { Type } from 'class-transformer';
import { BeneficiaryGender, BeneficiaryStatus, BeneficiaryType } from '../../domain/enums/beneficiary.enum';

export class CreateBeneficiaryDto {
  @IsString() @ApiProperty({ example: 'Asha Verma' }) name: string;
  @IsEnum(BeneficiaryType) @ApiProperty({ enum: BeneficiaryType, example: BeneficiaryType.INDIVIDUAL }) type: BeneficiaryType;
  @IsDate() @Type(() => Date) @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) enrollmentDate: Date;
  @IsOptional() @IsEnum(BeneficiaryGender) @ApiPropertyOptional({ enum: BeneficiaryGender, example: BeneficiaryGender.FEMALE }) gender?: BeneficiaryGender;
  @IsOptional() @IsNumber() @Min(0) @ApiPropertyOptional({ example: 34 }) age?: number;
  @IsOptional() @IsDate() @Type(() => Date) @ApiPropertyOptional({ example: '1992-03-14' }) dateOfBirth?: Date;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: '+919876543210' }) contactNumber?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'asha.verma@example.org' }) email?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'House 42, Nabapally, Barasat, West Bengal 700126' }) address?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Barasat, West Bengal' }) location?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Adult literacy' }) category?: string;
  @IsOptional() @ApiPropertyOptional({ example: ['Learning kit', 'Evening literacy classes'] }) benefitsReceived?: string[];
  @IsOptional() @ApiPropertyOptional({ example: 'Attends evening literacy classes regularly' }) notes?: string;
  @IsOptional() @ApiPropertyOptional({ example: { fundingSource: 'CSR grant' } }) metadata?: Record<string, unknown>;
}

export class UpdateBeneficiaryDto {
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Asha Verma' }) name?: string;
  @IsOptional() @IsEnum(BeneficiaryType) @ApiPropertyOptional({ enum: BeneficiaryType, example: BeneficiaryType.INDIVIDUAL }) type?: BeneficiaryType;
  @IsOptional() @IsEnum(BeneficiaryGender) @ApiPropertyOptional({ enum: BeneficiaryGender, example: BeneficiaryGender.FEMALE }) gender?: BeneficiaryGender;
  @IsOptional() @IsNumber() @Min(0) @ApiPropertyOptional({ example: 34 }) age?: number;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: '+919876543210' }) contactNumber?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'asha.verma@example.org' }) email?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'House 42, Nabapally, Barasat, West Bengal 700126' }) address?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Barasat, West Bengal' }) location?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Adult literacy' }) category?: string;
  @IsOptional() @ApiPropertyOptional({ example: ['Learning kit', 'Evening literacy classes'] }) benefitsReceived?: string[];
  @IsOptional() @ApiPropertyOptional({ example: 'Attends evening literacy classes regularly' }) notes?: string;
}

export class BeneficiaryDetailFilterDto {
  @IsOptional() @IsEnum(BeneficiaryStatus) @ApiPropertyOptional({ enum: BeneficiaryStatus, example: BeneficiaryStatus.ACTIVE }) status?: BeneficiaryStatus;
  @IsOptional() @IsEnum(BeneficiaryType) @ApiPropertyOptional({ enum: BeneficiaryType, example: BeneficiaryType.INDIVIDUAL }) type?: BeneficiaryType;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Adult literacy' }) category?: string;
}

export class BeneficiaryDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' }) id: string;
  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' }) projectId: string;
  @ApiProperty({ example: 'Asha Verma' }) name: string;
  @ApiProperty({ enum: BeneficiaryType, example: BeneficiaryType.INDIVIDUAL }) type: BeneficiaryType;
  @ApiPropertyOptional({ enum: BeneficiaryGender, example: BeneficiaryGender.FEMALE }) gender?: BeneficiaryGender;
  @ApiPropertyOptional({ example: 34 }) age?: number;
  @ApiPropertyOptional({ example: '1992-03-14' }) dateOfBirth?: Date;
  @ApiPropertyOptional({ example: '+919876543210' }) contactNumber?: string;
  @ApiPropertyOptional({ example: 'asha.verma@example.org' }) email?: string;
  @ApiPropertyOptional({ example: 'House 42, Nabapally, Barasat, West Bengal 700126' }) address?: string;
  @ApiPropertyOptional({ example: 'Barasat, West Bengal' }) location?: string;
  @ApiPropertyOptional({ example: 'Adult literacy' }) category?: string;
  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) enrollmentDate: Date;
  @ApiPropertyOptional({ example: '2026-12-18T17:00:00.000Z' }) exitDate?: Date;
  @ApiProperty({ enum: BeneficiaryStatus, example: BeneficiaryStatus.ACTIVE }) status: BeneficiaryStatus;
  @ApiProperty({ type: [String], example: ['Learning kit', 'Evening literacy classes'] }) benefitsReceived: string[];
  @ApiPropertyOptional({ example: 'Attends evening literacy classes regularly' }) notes?: string;
  @ApiPropertyOptional({ example: { fundingSource: 'CSR grant' } }) metadata?: Record<string, unknown>;
  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) createdAt: Date;
  @ApiProperty({ example: '2026-06-01T12:00:00.000Z' }) updatedAt: Date;
}

export class BeneficiaryListResponseDto {
  @ApiProperty({ type: [BeneficiaryDetailDto] }) items: BeneficiaryDetailDto[];
  @ApiProperty({ example: 312 }) total: number;
  @ApiProperty({ example: 0 }) pageIndex: number;
  @ApiProperty({ example: 20 }) pageSize: number;
}
