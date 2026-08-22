import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsArray, IsBoolean, IsDate, IsEnum, IsNumber, IsOptional, IsString, Min } from 'class-validator';
import { ProjectCategory, ProjectPhase, ProjectStatus } from '../../domain/enums/project.enum';
import { KeyValueOption } from '../ports/project-reference-data.port';

export class CreateProjectDto {
  @IsString() @ApiProperty({ example: 'Rural Literacy Drive 2026' }) name!: string;
  @IsString() @ApiProperty({ example: 'Community literacy programme for out-of-school children across Barasat block' }) description!: string;
  @IsString() @ApiProperty({ example: 'PRJ-2026-014' }) code!: string;
  @IsEnum(ProjectCategory) @ApiProperty({ enum: ProjectCategory, example: ProjectCategory.EDUCATION }) category!: ProjectCategory;
  @IsOptional() @IsEnum(ProjectStatus) @ApiPropertyOptional({ enum: ProjectStatus, example: ProjectStatus.ACTIVE }) status?: ProjectStatus;
  @IsOptional() @IsEnum(ProjectPhase) @ApiPropertyOptional({ enum: ProjectPhase, example: ProjectPhase.EXECUTION }) phase?: ProjectPhase;
  @IsDate() @Type(() => Date) @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) startDate!: Date;
  @IsOptional() @IsDate() @Type(() => Date) @ApiPropertyOptional({ example: '2026-12-18T17:00:00.000Z' }) endDate?: Date;
  @IsNumber() @Min(0.01) @ApiProperty({ example: 250000 }) budget!: number;
  @IsString() @ApiProperty({ example: 'INR' }) currency!: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Barasat, West Bengal' }) location?: string;
  @IsOptional() @IsNumber() @Min(0) @ApiPropertyOptional({ example: 500 }) targetBeneficiaryCount?: number;
  @IsString() @ApiProperty({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) managerId!: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) sponsorId?: string;
  @IsOptional() @IsArray() @IsString({ each: true }) @ApiPropertyOptional({ type: [String], example: ['education', 'rural'] }) tags?: string[];
  @IsOptional() @IsBoolean() @ApiPropertyOptional({ example: true }) isPublic?: boolean;
  @IsOptional() @ApiPropertyOptional({ example: { fundingSource: 'CSR grant' } }) metadata?: Record<string, unknown>;
}

export class UpdateProjectDto {
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Rural Literacy Drive 2026' }) name?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Community literacy programme for out-of-school children across Barasat block' }) description?: string;
  @IsOptional() @IsEnum(ProjectCategory) @ApiPropertyOptional({ enum: ProjectCategory, example: ProjectCategory.EDUCATION }) category?: ProjectCategory;
  @IsOptional() @IsEnum(ProjectStatus) @ApiPropertyOptional({ enum: ProjectStatus, example: ProjectStatus.ACTIVE }) status?: ProjectStatus;
  @IsOptional() @IsEnum(ProjectPhase) @ApiPropertyOptional({ enum: ProjectPhase, example: ProjectPhase.EXECUTION }) phase?: ProjectPhase;
  @IsOptional() @IsDate() @Type(() => Date) @ApiPropertyOptional({ example: '2026-12-18T17:00:00.000Z' }) endDate?: Date;
  @IsOptional() @IsNumber() @Min(0.01) @ApiPropertyOptional({ example: 250000 }) budget?: number;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Barasat, West Bengal' }) location?: string;
  @IsOptional() @IsNumber() @Min(0) @ApiPropertyOptional({ example: 500 }) targetBeneficiaryCount?: number;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) sponsorId?: string;
  @IsOptional() @IsArray() @IsString({ each: true }) @ApiPropertyOptional({ type: [String], example: ['education', 'rural'] }) tags?: string[];
  @IsOptional() @IsBoolean() @ApiPropertyOptional({ example: true }) isPublic?: boolean;
  @IsOptional() @ApiPropertyOptional({ example: { fundingSource: 'CSR grant' } }) metadata?: Record<string, unknown>;
}

export class ProjectDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' }) id!: string;
  @ApiProperty({ example: 'Rural Literacy Drive 2026' }) name!: string;
  @ApiProperty({ example: 'Community literacy programme for out-of-school children across Barasat block' }) description!: string;
  @ApiProperty({ example: 'PRJ-2026-014' }) code!: string;
  @ApiProperty({ enum: ProjectCategory, example: ProjectCategory.EDUCATION }) category!: ProjectCategory;
  @ApiProperty({ enum: ProjectStatus, example: ProjectStatus.ACTIVE }) status!: ProjectStatus;
  @ApiProperty({ enum: ProjectPhase, example: ProjectPhase.EXECUTION }) phase!: ProjectPhase;
  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) startDate!: Date;
  @ApiPropertyOptional({ example: '2026-12-18T17:00:00.000Z' }) endDate?: Date;
  @ApiPropertyOptional({ example: '2026-12-18T17:00:00.000Z' }) actualEndDate?: Date;
  @ApiProperty({ example: 250000 }) budget!: number;
  @ApiProperty({ example: 48500 }) spentAmount!: number;
  @ApiProperty({ example: 'INR' }) currency!: string;
  @ApiPropertyOptional({ example: 'Barasat, West Bengal' }) location?: string;
  @ApiPropertyOptional({ example: 500 }) targetBeneficiaryCount?: number;
  @ApiPropertyOptional({ example: 312 }) actualBeneficiaryCount?: number;
  @ApiProperty({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) managerId!: string;
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) sponsorId?: string;
  @ApiProperty({ type: [String], example: ['education', 'rural'] }) tags!: string[];
  @ApiProperty({ example: true }) isPublic!: boolean;
  @ApiPropertyOptional({ example: { fundingSource: 'CSR grant' } }) metadata?: Record<string, unknown>;
  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) createdAt!: Date;
  @ApiProperty({ example: '2026-06-01T12:00:00.000Z' }) updatedAt!: Date;
  @ApiProperty({ enum: ProjectStatus, isArray: true, example: [ProjectStatus.ON_HOLD, ProjectStatus.COMPLETED] }) nextStatus!: ProjectStatus[];
}

export class ProjectDetailFilterDto {
  @ApiPropertyOptional({ enum: ProjectStatus, example: ProjectStatus.ACTIVE }) status?: ProjectStatus;
  @ApiPropertyOptional({ enum: ProjectCategory, example: ProjectCategory.EDUCATION }) category?: ProjectCategory;
  @ApiPropertyOptional({ enum: ProjectPhase, example: ProjectPhase.EXECUTION }) phase?: ProjectPhase;
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) managerId?: string;
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) sponsorId?: string;
  @ApiPropertyOptional({ example: 'Barasat, West Bengal' }) location?: string;
  @ApiPropertyOptional({ type: [String], example: ['education', 'rural'] }) tags?: string[];
  @ApiPropertyOptional({ example: true }) isPublic?: boolean;
}

export class ProjectRefDataDto {
  @ApiProperty({ isArray: true, example: [{ key: 'EDUCATION', value: 'Education' }] }) projectCategories!: KeyValueOption[];
  @ApiProperty({ isArray: true, example: [{ key: 'ACTIVE', value: 'Active' }] }) projectStatuses!: KeyValueOption[];
  @ApiProperty({ isArray: true, example: [{ key: 'EXECUTION', value: 'Execution' }] }) projectPhases!: KeyValueOption[];
  @ApiProperty({ isArray: true, example: [{ key: 'ACTIVITY', value: 'Activity' }] }) activityScales!: KeyValueOption[];
  @ApiProperty({ isArray: true, example: [{ key: 'TRAINING', value: 'Training' }] }) activityTypes!: KeyValueOption[];
  @ApiProperty({ isArray: true, example: [{ key: 'IN_PROGRESS', value: 'In progress' }] }) activityStatuses!: KeyValueOption[];
  @ApiProperty({ isArray: true, example: [{ key: 'HIGH', value: 'High' }] }) activityPriorities!: KeyValueOption[];
}
