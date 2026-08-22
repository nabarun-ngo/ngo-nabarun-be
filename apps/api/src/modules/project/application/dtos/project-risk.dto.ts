import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsDate, IsEnum, IsOptional, IsString, IsUUID } from 'class-validator';
import {
  RiskCategory,
  RiskProbability,
  RiskSeverity,
  RiskStatus,
} from '../../domain/enums/risk.enum';

export class ProjectRiskDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  id!: string;

  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  projectId!: string;

  @ApiProperty({ example: 'Monsoon delays may stall construction' })
  title!: string;

  @ApiPropertyOptional({
    nullable: true,
    example: 'Heavy rainfall between June and August could halt site work for several weeks.',
  })
  description?: string | null;

  @ApiProperty({ enum: RiskCategory, example: RiskCategory.TIMELINE })
  category!: RiskCategory;

  @ApiProperty({ enum: RiskSeverity, example: RiskSeverity.HIGH })
  severity!: RiskSeverity;

  @ApiProperty({ enum: RiskProbability, example: RiskProbability.MEDIUM })
  probability!: RiskProbability;

  @ApiProperty({ enum: RiskStatus, example: RiskStatus.MONITORING })
  status!: RiskStatus;

  @ApiPropertyOptional({ nullable: true, example: 'Up to six weeks of schedule slippage.' })
  impact?: string | null;

  @ApiPropertyOptional({
    nullable: true,
    example: 'Front-load foundation work before June and keep a covered work area on site.',
  })
  mitigationPlan?: string | null;

  @ApiPropertyOptional({ nullable: true, example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  ownerId?: string | null;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-10T00:00:00.000Z' })
  identifiedDate!: Date;

  @ApiPropertyOptional({
    type: String,
    format: 'date-time',
    nullable: true,
    example: '2026-09-01T00:00:00.000Z',
  })
  resolvedDate?: Date | null;

  @ApiPropertyOptional({ nullable: true, example: 'Reviewed at the March steering committee.' })
  notes?: string | null;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  createdAt!: Date;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  updatedAt!: Date;
}

export class ProjectRiskListResponseDto {
  @ApiProperty({ type: () => [ProjectRiskDetailDto] })
  items!: ProjectRiskDetailDto[];

  @ApiProperty({ example: 3 })
  total!: number;
}

export class CreateProjectRiskDto {
  @ApiProperty({ example: 'Monsoon delays may stall construction' })
  @IsString()
  title!: string;

  @ApiProperty({ enum: RiskCategory, example: RiskCategory.TIMELINE })
  @IsEnum(RiskCategory)
  category!: RiskCategory;

  @ApiProperty({ enum: RiskSeverity, example: RiskSeverity.HIGH })
  @IsEnum(RiskSeverity)
  severity!: RiskSeverity;

  @ApiProperty({ enum: RiskProbability, example: RiskProbability.MEDIUM })
  @IsEnum(RiskProbability)
  probability!: RiskProbability;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-10T00:00:00.000Z' })
  @IsDate()
  @Type(() => Date)
  identifiedDate!: Date;

  @ApiPropertyOptional({
    example: 'Heavy rainfall between June and August could halt site work for several weeks.',
  })
  @IsOptional()
  @IsString()
  description?: string;

  @ApiPropertyOptional({ example: 'Up to six weeks of schedule slippage.' })
  @IsOptional()
  @IsString()
  impact?: string;

  @ApiPropertyOptional({
    example: 'Front-load foundation work before June and keep a covered work area on site.',
  })
  @IsOptional()
  @IsString()
  mitigationPlan?: string;

  @ApiPropertyOptional({ format: 'uuid', example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @IsOptional()
  @IsUUID()
  ownerId?: string;
}

export class UpdateProjectRiskDto {
  @ApiPropertyOptional({ example: 'Monsoon delays may stall construction and procurement' })
  @IsOptional()
  @IsString()
  title?: string;

  @ApiPropertyOptional({ enum: RiskSeverity, example: RiskSeverity.CRITICAL })
  @IsOptional()
  @IsEnum(RiskSeverity)
  severity?: RiskSeverity;

  @ApiPropertyOptional({ enum: RiskProbability, example: RiskProbability.HIGH })
  @IsOptional()
  @IsEnum(RiskProbability)
  probability?: RiskProbability;

  @ApiPropertyOptional({ example: 'Added a contingency vendor for covered storage.' })
  @IsOptional()
  @IsString()
  mitigationPlan?: string;

  @ApiPropertyOptional({ enum: RiskStatus, example: RiskStatus.MITIGATED })
  @IsOptional()
  @IsEnum(RiskStatus)
  status?: RiskStatus;
}
