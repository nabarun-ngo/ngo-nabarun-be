import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsDate, IsEnum, IsNumber, IsOptional, IsString, IsUUID, Min } from 'class-validator';
import { TeamMemberRole } from '../../domain/enums/team-member.enum';

export class TeamMemberDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  id!: string;

  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  projectId!: string;

  @ApiProperty({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  userId!: string;

  @ApiProperty({ enum: TeamMemberRole, example: TeamMemberRole.COORDINATOR })
  role!: TeamMemberRole;

  @ApiPropertyOptional({
    nullable: true,
    example: 'Coordinates volunteer rosters and weekly field reports.',
  })
  responsibilities?: string | null;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-01T00:00:00.000Z' })
  startDate!: Date;

  @ApiPropertyOptional({
    type: String,
    format: 'date-time',
    nullable: true,
    example: '2026-12-31T00:00:00.000Z',
  })
  endDate?: Date | null;

  @ApiPropertyOptional({ nullable: true, example: 20 })
  hoursAllocated?: number | null;

  @ApiProperty({ example: true })
  isActive!: boolean;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  createdAt!: Date;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  updatedAt!: Date;
}

export class TeamMemberListResponseDto {
  @ApiProperty({ type: () => [TeamMemberDetailDto] })
  items!: TeamMemberDetailDto[];

  @ApiProperty({ example: 6 })
  total!: number;
}

export class AddTeamMemberDto {
  @ApiProperty({ format: 'uuid', example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @IsUUID()
  userId!: string;

  @ApiProperty({ enum: TeamMemberRole, example: TeamMemberRole.COORDINATOR })
  @IsEnum(TeamMemberRole)
  role!: TeamMemberRole;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-01T00:00:00.000Z' })
  @IsDate()
  @Type(() => Date)
  startDate!: Date;

  @ApiPropertyOptional({ example: 'Coordinates volunteer rosters and weekly field reports.' })
  @IsOptional()
  @IsString()
  responsibilities?: string;

  @ApiPropertyOptional({ example: 20 })
  @IsOptional()
  @IsNumber()
  @Min(0)
  hoursAllocated?: number;
}

export class UpdateTeamMemberDto {
  @ApiPropertyOptional({ enum: TeamMemberRole, example: TeamMemberRole.TEAM_LEAD })
  @IsOptional()
  @IsEnum(TeamMemberRole)
  role?: TeamMemberRole;

  @ApiPropertyOptional({ example: 'Now also owns stakeholder reporting.' })
  @IsOptional()
  @IsString()
  responsibilities?: string;

  @ApiPropertyOptional({ example: 30 })
  @IsOptional()
  @IsNumber()
  @Min(0)
  hoursAllocated?: number;
}
