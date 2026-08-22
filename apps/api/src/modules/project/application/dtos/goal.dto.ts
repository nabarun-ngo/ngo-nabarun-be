import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsDate, IsEnum, IsNumber, IsOptional, IsString, Min } from 'class-validator';
import { Type } from 'class-transformer';
import { GoalPriority, GoalStatus } from '../../domain/enums/goal.enum';
export class CreateGoalDto {
  @IsString() @ApiProperty({ example: 'Enrol 500 children in primary school' }) title: string;
  @IsEnum(GoalPriority) @ApiProperty({ enum: GoalPriority, example: GoalPriority.HIGH }) priority: GoalPriority;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Enrol out-of-school children from Barasat block into nearby government primary schools' }) description?: string;
  @IsOptional() @IsNumber() @Min(0) @ApiPropertyOptional({ example: 500 }) targetValue?: number;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'children' }) targetUnit?: string;
  @IsOptional() @IsDate() @Type(() => Date) @ApiPropertyOptional({ example: '2026-12-18T17:00:00.000Z' }) deadline?: Date;
  @IsOptional() @IsNumber() @Min(0) @ApiPropertyOptional({ example: 40 }) weight?: number;
}
export class UpdateGoalDto {
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Enrol 600 children in primary school' }) title?: string;
  @IsOptional() @IsString() @ApiPropertyOptional({ example: 'Scope widened to include two additional blocks' }) description?: string;
  @IsOptional() @IsNumber() @Min(0) @ApiPropertyOptional({ example: 600 }) targetValue?: number;
  @IsOptional() @IsEnum(GoalPriority) @ApiPropertyOptional({ enum: GoalPriority, example: GoalPriority.CRITICAL }) priority?: GoalPriority;
}
export class UpdateGoalProgressDto { @IsNumber() @Min(0) @ApiProperty({ example: 312 }) currentValue: number; }
export class GoalDetailDto { @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' }) id: string; @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' }) projectId: string; @ApiProperty({ example: 'Enrol 500 children in primary school' }) title: string; @ApiProperty({ example: GoalPriority.HIGH }) priority: GoalPriority; @ApiProperty({ example: GoalStatus.IN_PROGRESS }) status: GoalStatus; @ApiProperty({ example: 312 }) currentValue: number; @ApiPropertyOptional({ example: 500 }) targetValue?: number; @ApiPropertyOptional({ example: 'Enrol out-of-school children from Barasat block into nearby government primary schools' }) description?: string; }
export class GoalListResponseDto { @ApiProperty({ type: [GoalDetailDto] }) items: GoalDetailDto[]; @ApiProperty({ example: 12 }) total: number; @ApiProperty({ example: 0 }) pageIndex: number; @ApiProperty({ example: 20 }) pageSize: number; }
