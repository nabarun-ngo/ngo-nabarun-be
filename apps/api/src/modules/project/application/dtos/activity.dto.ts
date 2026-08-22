import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsArray, IsDate, IsEnum, IsInt, IsNumber, IsOptional, IsString, Min } from 'class-validator';
import { ActivityPriority, ActivityScale, ActivityStatus, ActivityType } from '../../domain/enums/activity.enum';

export class CreateActivityDto {
  @IsString() @ApiProperty({ example: 'Teacher training workshop' }) name!: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Two-day training for government primary school teachers on phonics-based reading' }) description?: string;
  @IsEnum(ActivityScale) @ApiProperty({ enum: ActivityScale, example: ActivityScale.ACTIVITY }) scale!: ActivityScale;
  @IsEnum(ActivityType) @ApiProperty({ enum: ActivityType, example: ActivityType.TRAINING }) type!: ActivityType;
  @IsEnum(ActivityPriority) @ApiProperty({ enum: ActivityPriority, example: ActivityPriority.HIGH }) priority!: ActivityPriority;
  @IsOptional() @IsDate() @Type(() => Date) @ApiPropertyOptional({ example: '2026-03-14T09:30:00.000Z' }) startDate?: Date;
  @IsOptional() @IsDate() @Type(() => Date) @ApiPropertyOptional({ example: '2026-03-15T17:00:00.000Z' }) endDate?: Date;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Barasat, West Bengal' }) location?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Barasat Government Primary School' }) venue?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) assignedTo?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) organizerId?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' }) parentActivityId?: string;
  @IsOptional() @IsInt() @Min(0) @ApiPropertyOptional({ example: 40 }) expectedParticipants?: number;
  @IsOptional() @IsNumber() @Min(0.01) @ApiPropertyOptional({ example: 48500 }) estimatedCost?: number;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'INR' }) currency?: string;
  @IsOptional() @IsArray() @IsString({ each: true }) @ApiPropertyOptional({ type: [String], example: ['education', 'rural'] }) tags?: string[];
  @IsOptional() @ApiPropertyOptional({ example: { fundingSource: 'CSR grant' } }) metadata?: Record<string, unknown>;
}

export class UpdateActivityDto {
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Teacher training workshop' }) name?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Two-day training for government primary school teachers on phonics-based reading' }) description?: string;
  @IsOptional() @IsEnum(ActivityType) @ApiPropertyOptional({ enum: ActivityType, example: ActivityType.TRAINING }) type?: ActivityType;
  @IsOptional() @IsEnum(ActivityStatus) @ApiPropertyOptional({ enum: ActivityStatus, example: ActivityStatus.IN_PROGRESS }) status?: ActivityStatus;
  @IsOptional() @IsEnum(ActivityPriority) @ApiPropertyOptional({ enum: ActivityPriority, example: ActivityPriority.HIGH }) priority?: ActivityPriority;
  @IsOptional() @IsDate() @Type(() => Date) @ApiPropertyOptional({ example: '2026-03-14T09:30:00.000Z' }) startDate?: Date;
  @IsOptional() @IsDate() @Type(() => Date) @ApiPropertyOptional({ example: '2026-03-15T17:00:00.000Z' }) endDate?: Date;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Barasat, West Bengal' }) location?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Barasat Government Primary School' }) venue?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) assignedTo?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) organizerId?: string;
  @IsOptional() @IsInt() @Min(0) @ApiPropertyOptional({ example: 40 }) expectedParticipants?: number;
  @IsOptional() @IsNumber() @Min(0.01) @ApiPropertyOptional({ example: 48500 }) estimatedCost?: number;
  @IsOptional() @IsArray() @IsString({ each: true }) @ApiPropertyOptional({ type: [String], example: ['education', 'rural'] }) tags?: string[];
  @IsOptional() @ApiPropertyOptional({ example: { fundingSource: 'CSR grant' } }) metadata?: Record<string, unknown>;
}

export class LinkExpenseToActivityDto {
  @IsString() @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' }) expenseId!: string;
}

export class ActivityDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' }) id!: string;
  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' }) projectId!: string;
  @ApiProperty({ example: 'Teacher training workshop' }) name!: string;
  @ApiPropertyOptional({ example: 'Two-day training for government primary school teachers on phonics-based reading' }) description?: string;
  @ApiProperty({ enum: ActivityScale, example: ActivityScale.ACTIVITY }) scale!: ActivityScale;
  @ApiProperty({ enum: ActivityType, example: ActivityType.TRAINING }) type!: ActivityType;
  @ApiProperty({ enum: ActivityStatus, example: ActivityStatus.IN_PROGRESS }) status!: ActivityStatus;
  @ApiProperty({ enum: ActivityPriority, example: ActivityPriority.HIGH }) priority!: ActivityPriority;
  @ApiPropertyOptional({ example: '2026-03-14T09:30:00.000Z' }) startDate?: Date;
  @ApiPropertyOptional({ example: '2026-03-15T17:00:00.000Z' }) endDate?: Date;
  @ApiPropertyOptional({ example: '2026-03-14T09:30:00.000Z' }) actualStartDate?: Date;
  @ApiPropertyOptional({ example: '2026-03-15T17:00:00.000Z' }) actualEndDate?: Date;
  @ApiPropertyOptional({ example: 'Barasat, West Bengal' }) location?: string;
  @ApiPropertyOptional({ example: 'Barasat Government Primary School' }) venue?: string;
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) assignedTo?: string;
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) organizerId?: string;
  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' }) parentActivityId?: string;
  @ApiPropertyOptional({ example: 40 }) expectedParticipants?: number;
  @ApiPropertyOptional({ example: 36 }) actualParticipants?: number;
  @ApiPropertyOptional({ example: 48500 }) estimatedCost?: number;
  @ApiPropertyOptional({ example: 45200 }) actualCost?: number;
  @ApiPropertyOptional({ example: 'INR' }) currency?: string;
  @ApiProperty({ type: [String], example: ['education', 'rural'] }) tags!: string[];
  @ApiPropertyOptional({ example: { fundingSource: 'CSR grant' } }) metadata?: Record<string, unknown>;
  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) createdAt!: Date;
  @ApiProperty({ example: '2026-06-01T12:00:00.000Z' }) updatedAt!: Date;
  @ApiProperty({ enum: ActivityStatus, isArray: true, example: [ActivityStatus.COMPLETED, ActivityStatus.CANCELLED] }) nextStatus!: ActivityStatus[];
}

export class ActivityDetailFilterDto {
  @IsOptional() @IsString() @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' }) projectId?: string;
  @IsOptional() @IsEnum(ActivityScale) @ApiPropertyOptional({ enum: ActivityScale, example: ActivityScale.ACTIVITY }) scale?: ActivityScale;
  @ApiPropertyOptional({ enum: ActivityStatus, example: ActivityStatus.IN_PROGRESS }) status?: ActivityStatus;
  @ApiPropertyOptional({ enum: ActivityType, example: ActivityType.TRAINING }) type?: ActivityType;
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) assignedTo?: string;
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) organizerId?: string;
  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' }) parentActivityId?: string;
}
