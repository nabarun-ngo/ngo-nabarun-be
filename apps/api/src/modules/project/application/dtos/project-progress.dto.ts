import { ApiProperty } from '@nestjs/swagger';
import { ProjectDetailDto } from './project.dto';

/**
 * Swagger models for the progress and dashboard read endpoints.
 *
 * The handlers declare their results as TypeScript interfaces, which carry no
 * runtime metadata and so cannot be used as OpenAPI models. These classes
 * mirror those interfaces field for field.
 */
export class ProjectProgressResponseDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  projectId!: string;

  @ApiProperty({ description: 'Share of budget spent, as a percentage', example: 62.5 })
  budgetUtilization!: number;

  @ApiProperty({ example: 156250 })
  spentAmount!: number;

  @ApiProperty({ example: 250000 })
  budget!: number;

  @ApiProperty({ example: 412 })
  beneficiaryCount!: number;

  @ApiProperty({ example: 18 })
  activityCount!: number;

  @ApiProperty({ example: 5 })
  goalCount!: number;

  @ApiProperty({ example: 4 })
  milestoneCount!: number;

  @ApiProperty({ description: 'Risks not yet closed', example: 3 })
  openRiskCount!: number;
}

export class ActivitySummaryDto {
  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  id!: string;

  @ApiProperty({ example: 'Teacher training workshop' })
  name!: string;

  @ApiProperty({ example: 'IN_PROGRESS' })
  status!: string;

  @ApiProperty({ example: 'MEDIUM' })
  scale!: string;
}

export class MilestoneSummaryDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  id!: string;

  @ApiProperty({ example: 'Enrol first 200 children' })
  name!: string;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-06-30T00:00:00.000Z' })
  targetDate!: Date;

  @ApiProperty({ example: 'IN_PROGRESS' })
  status!: string;
}

export class ProjectDashboardResponseDto {
  @ApiProperty({ type: () => ProjectDetailDto })
  project!: ProjectDetailDto;

  @ApiProperty({ type: () => ProjectProgressResponseDto })
  progress!: ProjectProgressResponseDto;

  @ApiProperty({ type: () => [ActivitySummaryDto] })
  recentActivities!: ActivitySummaryDto[];

  @ApiProperty({ type: () => [MilestoneSummaryDto] })
  upcomingMilestones!: MilestoneSummaryDto[];
}
