import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { HELP_ARTICLE_BLOCK_TYPES, HELP_CALLOUT_TONES } from '../../help-portal.schema';

export class HelpPortalCategoryDto {
  @ApiProperty({ example: 'members' }) key!: string;
  @ApiProperty({ example: 'Members' }) title!: string;
  @ApiPropertyOptional({ example: 1 }) order?: number;
}

export class HelpPortalArticleSummaryDto {
  @ApiProperty({ example: 'complete-member-profile' }) slug!: string;
  @ApiProperty({ example: 'Complete a member profile' }) title!: string;
  @ApiProperty({ example: 'members' }) categoryKey!: string;
  @ApiPropertyOptional({ example: 'Fill required fields and save.' }) summary?: string;
  @ApiPropertyOptional({ example: 1 }) order?: number;
  @ApiProperty({ example: true }) active!: boolean;
  @ApiPropertyOptional({ example: 5 }) estimatedMinutes?: number;
}

export class HelpPortalCatalogDto {
  @ApiProperty({ type: HelpPortalCategoryDto, isArray: true })
  categories!: HelpPortalCategoryDto[];
  @ApiProperty({ type: String, isArray: true, example: ['getting-started'] })
  featuredSlugs!: string[];
  @ApiProperty({ type: HelpPortalArticleSummaryDto, isArray: true })
  articles!: HelpPortalArticleSummaryDto[];
}

export class HelpArticleBlockDto {
  @ApiProperty({ enum: HELP_ARTICLE_BLOCK_TYPES, example: 'paragraph' })
  type!: string;
  @ApiPropertyOptional({ example: 'Before you start' }) text?: string;
  @ApiPropertyOptional({ example: 2 }) level?: number;
  @ApiPropertyOptional({ enum: HELP_CALLOUT_TONES, example: 'tip' }) tone?: string;
  @ApiPropertyOptional({ type: String, isArray: true }) items?: string[];
  @ApiPropertyOptional({ example: 'https://www.youtube.com/watch?v=example' }) url?: string;
  @ApiPropertyOptional({ example: 'Walkthrough' }) title?: string;
  @ApiPropertyOptional({ example: 'Policy PDF' }) label?: string;
  @ApiPropertyOptional({ example: true }) external?: boolean;
}

export class HelpPortalArticleDto {
  @ApiProperty({ example: 'complete-member-profile' }) slug!: string;
  @ApiProperty({ example: 'Complete a member profile' }) title!: string;
  @ApiProperty({ example: 'members' }) categoryKey!: string;
  @ApiPropertyOptional({ example: 'Fill required fields and save.' }) summary?: string;
  @ApiPropertyOptional({ example: '2026-08-15' }) updatedAt?: string;
  @ApiProperty({ type: String, isArray: true }) relatedSlugs!: string[];
  @ApiProperty({ type: HelpArticleBlockDto, isArray: true }) blocks!: HelpArticleBlockDto[];
}
