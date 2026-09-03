import { IsEmail, IsEnum, IsNumber, IsOptional, IsString, Min, IsIn, IsDate } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { PaginatedQueryDto } from '@nabarun-ngo/nestjs-shared-core';
import { DonorStatus } from '../../domain/enums/donor-status.enum';
import { DonorType } from '../../domain/enums/donor-type.enum';

const DONOR_SORT_FIELDS = ['fullName', 'email', 'status', 'type', 'createdAt', 'updatedAt'] as const;

export class DonorReferenceOptionDto {
  @ApiProperty()
  key!: string;

  @ApiProperty()
  value!: string;

  @ApiPropertyOptional()
  description?: string;
}

export class DonorRefDataDto {
  @ApiProperty({ type: () => [DonorReferenceOptionDto] })
  donorStatuses?: DonorReferenceOptionDto[];

  @ApiProperty({ type: () => [DonorReferenceOptionDto] })
  memberEditableDonorStatuses?: DonorReferenceOptionDto[];

  @ApiPropertyOptional({ type: [String], example: ['PAUSED', 'WAIVED'] })
  statusesRequiringEndDate?: string[];
}

export class CreateGuestDonorRequestDto {
  @ApiProperty({ example: 'Asha Verma' })
  @IsString()
  fullName!: string;

  @ApiPropertyOptional({ example: 'asha.verma@example.org' })
  @IsOptional()
  @IsEmail()
  email?: string;

  @ApiPropertyOptional({ example: '+91' })
  @IsOptional()
  @IsString()
  phoneCode?: string;

  @ApiPropertyOptional({ example: '9876543210' })
  @IsOptional()
  @IsString()
  phoneNumber?: string;

  @ApiPropertyOptional({ example: 500 })
  @IsOptional()
  @IsNumber()
  @Min(1)
  preferredAmount?: number;
}

export class UpdateGuestDonorRequestDto {
  @ApiPropertyOptional({ example: 'Asha Verma' })
  @IsOptional()
  @IsString()
  fullName?: string;

  @ApiPropertyOptional({ example: 'asha.verma@example.org' })
  @IsOptional()
  @IsEmail()
  email?: string;

  @ApiPropertyOptional({ example: '+91' })
  @IsOptional()
  @IsString()
  phoneCode?: string;

  @ApiPropertyOptional({ example: '9876543210' })
  @IsOptional()
  @IsString()
  phoneNumber?: string;
}

export class UpdateMemberDonorRequestDto {
  @ApiPropertyOptional({ example: 500 })
  @IsOptional()
  @IsNumber()
  @Min(1)
  preferredAmount?: number;

  @ApiPropertyOptional({ enum: DonorStatus })
  @IsOptional()
  @IsEnum(DonorStatus)
  status?: DonorStatus;

  @ApiPropertyOptional({ type: String, format: 'date-time' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  statusEndDate?: Date;
}

export class MergeGuestDonorsRequestDto {
  @ApiProperty({ example: 'source-donor-uuid' })
  @IsString()
  sourceDonorId!: string;

  @ApiProperty({ example: 'target-donor-uuid' })
  @IsString()
  targetDonorId!: string;
}

export class ListDonorsQueryDto extends PaginatedQueryDto {
  @ApiPropertyOptional({ description: 'Generic search across name, email, phone' })
  @IsOptional()
  @IsString()
  q?: string;

  @ApiPropertyOptional({ enum: DonorType })
  @IsOptional()
  @IsEnum(DonorType)
  type?: DonorType;

  @ApiPropertyOptional({ enum: DonorStatus })
  @IsOptional()
  @IsEnum(DonorStatus)
  status?: DonorStatus;

  @ApiPropertyOptional({ enum: DONOR_SORT_FIELDS, example: 'createdAt' })
  @IsOptional()
  @IsIn(DONOR_SORT_FIELDS)
  declare sortBy?: string;
}

export class DonorResponseDto {
  @ApiProperty()
  id!: string;

  @ApiProperty({ enum: DonorType })
  type!: DonorType;

  @ApiProperty({ enum: DonorStatus })
  status!: DonorStatus;

  @ApiPropertyOptional()
  preferredAmount?: number;

  @ApiPropertyOptional()
  statusEndDate?: Date;

  @ApiPropertyOptional()
  fullName?: string;

  @ApiPropertyOptional()
  email?: string;

  @ApiPropertyOptional()
  phoneCode?: string;

  @ApiPropertyOptional()
  phoneNumber?: string;

  @ApiPropertyOptional()
  userProfileId?: string;

  @ApiProperty()
  createdAt!: Date;

  @ApiProperty()
  updatedAt!: Date;
}
