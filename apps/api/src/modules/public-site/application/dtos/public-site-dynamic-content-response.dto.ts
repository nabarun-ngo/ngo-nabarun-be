import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class PublicSiteProjectGoalDto {
  @ApiProperty({ example: 'School enrolment support' })
  name!: string;

  @ApiPropertyOptional({ example: 'Support for enrolment processes' })
  description?: string;

  @ApiProperty({ example: true })
  active!: boolean;
}

export class PublicSiteProjectMetadataDto {
  @ApiPropertyOptional({ example: '/img/nbrn/pic1.jpg' })
  image?: string;

  @ApiPropertyOptional({ example: 'fas fa-graduation-cap' })
  icon?: string;

  @ApiPropertyOptional({
    description: 'Overlay heading; pair with beneficiaryCount on the client',
    example: 'Education Impact',
  })
  impactTitle?: string;

  @ApiPropertyOptional({
    description: 'Label under the beneficiary count on the card overlay',
    example: 'Children Supported',
  })
  impactLabel?: string;
}

export class PublicSiteProjectCardDto {
  @ApiProperty({ example: 'Child Education' })
  title!: string;

  @ApiProperty()
  description!: string;

  @ApiProperty({ type: [PublicSiteProjectGoalDto] })
  goals!: PublicSiteProjectGoalDto[];

  @ApiProperty({
    description: 'From project.actualBeneficiaryCount',
    example: 200,
  })
  beneficiaryCount!: number;

  @ApiProperty({ type: PublicSiteProjectMetadataDto })
  metadata!: PublicSiteProjectMetadataDto;
}

export class PublicSiteImpactStatsDto {
  @ApiProperty({ description: 'Hero metric: Lives Touched', example: 500 })
  beneficiaryCount!: number;

  @ApiProperty({ description: 'Hero metric: Projects', example: 15 })
  projectCount!: number;
}

export class PublicSiteEventDto {
  @ApiProperty() id!: string;
  @ApiProperty() title!: string;
  @ApiProperty() description!: string;
  @ApiProperty({ example: '2026-03-15' }) date!: string;
  @ApiPropertyOptional() endDate?: string;
  @ApiProperty() location!: string;
  @ApiProperty({ description: 'Parent public project name', example: 'Child Education' })
  projectName!: string;
  @ApiPropertyOptional() image?: string;
  @ApiPropertyOptional() registrationUrl?: string;
  @ApiPropertyOptional() active?: boolean;
}

export class PublicSiteTeamMemberDto {
  @ApiProperty() id!: string;
  @ApiProperty() fullName!: string;
  @ApiProperty() picture!: string;
  @ApiProperty() roleString!: string;
  @ApiProperty() email!: string;
  @ApiPropertyOptional() bio?: string;
  @ApiPropertyOptional() socialLinks?: Record<string, string>;
  @ApiPropertyOptional() active?: boolean;
}

export class PublicSiteDynamicContentResponseDto {
  @ApiProperty({ type: PublicSiteImpactStatsDto })
  stats!: PublicSiteImpactStatsDto;

  @ApiProperty({ type: [PublicSiteTeamMemberDto] })
  team!: PublicSiteTeamMemberDto[];

  @ApiProperty({ type: [PublicSiteProjectCardDto] })
  projects!: PublicSiteProjectCardDto[];

  @ApiProperty({ type: [PublicSiteEventDto] })
  events!: PublicSiteEventDto[];
}
