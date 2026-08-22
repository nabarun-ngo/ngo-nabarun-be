import { Controller, Get, Param, UseGuards } from '@nestjs/common';
import { QueryBus } from '@nestjs/cqrs';
import { ApiBearerAuth, ApiOperation, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { RequirePermissions, UnifiedAuthGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoResponse, ApiKeyParam } from '@nabarun-ngo/nestjs-shared-core';
import { GetHelpPortalCatalogQuery } from '../../application/queries/get-help-portal-catalog/get-help-portal-catalog.query';
import { GetHelpPortalArticleQuery } from '../../application/queries/get-help-portal-article/get-help-portal-article.query';
import { HelpPortalArticleDto, HelpPortalCatalogDto } from '../../application/dtos/help-portal.dto';

@ApiTags('Help Portal')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('help-portal')
export class HelpPortalController {
  constructor(private readonly queryBus: QueryBus) {}

  @Get('catalog')
  @RequirePermissions('read:help_portal')
  @ApiOperation({ summary: 'Get help portal catalog (categories, featured, article index)' })
  @ApiAutoResponse(HelpPortalCatalogDto)
  getCatalog(): Promise<HelpPortalCatalogDto> {
    return this.queryBus.execute(new GetHelpPortalCatalogQuery());
  }

  @Get('articles/:slug')
  @RequirePermissions('read:help_portal')
  @ApiOperation({ summary: 'Get a help article by slug, including content blocks' })
  @ApiKeyParam('slug', 'complete-member-profile', 'Article slug')
  @ApiAutoResponse(HelpPortalArticleDto)
  getArticle(@Param('slug') slug: string): Promise<HelpPortalArticleDto> {
    return this.queryBus.execute(new GetHelpPortalArticleQuery(slug));
  }
}
