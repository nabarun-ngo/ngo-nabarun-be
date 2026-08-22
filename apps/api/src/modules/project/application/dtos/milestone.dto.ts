import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsDate, IsEnum, IsOptional, IsString } from 'class-validator';
import { MilestoneImportance, MilestoneStatus } from '../../domain/enums/milestone.enum';

export class MilestoneDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  id!: string;

  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  projectId!: string;

  @ApiProperty({ example: 'Enrol first 200 children' })
  name!: string;

  @ApiPropertyOptional({
    nullable: true,
    example: 'Complete enrolment drive across the three pilot villages.',
  })
  description?: string | null;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-06-30T00:00:00.000Z' })
  targetDate!: Date;

  @ApiPropertyOptional({
    type: String,
    format: 'date-time',
    nullable: true,
    example: '2026-06-24T00:00:00.000Z',
  })
  actualDate?: Date | null;

  @ApiProperty({ enum: MilestoneStatus, example: MilestoneStatus.IN_PROGRESS })
  status!: MilestoneStatus;

  @ApiProperty({ enum: MilestoneImportance, example: MilestoneImportance.HIGH })
  importance!: MilestoneImportance;

  @ApiProperty({ type: [String], example: ['3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55'] })
  dependencies!: string[];

  @ApiPropertyOptional({ nullable: true, example: 'Blocked on district approval.' })
  notes?: string | null;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  createdAt!: Date;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  updatedAt!: Date;
}

export class MilestoneListResponseDto {
  @ApiProperty({ type: () => [MilestoneDetailDto] })
  items!: MilestoneDetailDto[];

  @ApiProperty({ example: 4 })
  total!: number;
}

export class CreateMilestoneDto {
  @ApiProperty({ example: 'Enrol first 200 children' })
  @IsString()
  name!: string;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-06-30T00:00:00.000Z' })
  @IsDate()
  @Type(() => Date)
  targetDate!: Date;

  @ApiProperty({ enum: MilestoneImportance, example: MilestoneImportance.HIGH })
  @IsEnum(MilestoneImportance)
  importance!: MilestoneImportance;

  @ApiPropertyOptional({ example: 'Complete enrolment drive across the three pilot villages.' })
  @IsOptional()
  @IsString()
  description?: string;
}

export class UpdateMilestoneDto {
  @ApiPropertyOptional({ example: 'Enrol first 250 children' })
  @IsOptional()
  @IsString()
  name?: string;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-07-15T00:00:00.000Z' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  targetDate?: Date;

  @ApiPropertyOptional({ enum: MilestoneImportance, example: MilestoneImportance.CRITICAL })
  @IsOptional()
  @IsEnum(MilestoneImportance)
  importance?: MilestoneImportance;

  @ApiPropertyOptional({ example: 'Scope widened to include two additional villages.' })
  @IsOptional()
  @IsString()
  description?: string;
}
