import { Controller, Get } from '@nestjs/common';
import { QueryBus } from '@nestjs/cqrs';
import { ApiOperation, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { ApiAutoResponse, BypassSuccessEnvelope } from '@nabarun-ngo/nestjs-shared-core';
import { RequirePermissions, UseApiKey } from '@nabarun-ngo/nestjs-shared-auth';
import { PublicSiteDynamicContentResponseDto } from '../../application/dtos/public-site-dynamic-content-response.dto';
import { PublicSiteDynamicContent } from '../../public-site.schema';
import { GetStaticContentQuery } from '../../application/queries/get-static-content/get-static-content.query';
import { GetDynamicContentQuery } from '../../application/queries/get-dynamic-content/get-dynamic-content.query';

@ApiTags('PublicSiteContents')
@Controller('public-site/contents')
@UseApiKey()
@ApiSecurity('api-key')
export class PublicSiteContentsController {
  constructor(private readonly queryBus: QueryBus) { }

  @Get('static')
  @ApiOperation({ summary: 'Get static public site content' })
  @RequirePermissions('read:public_content')
  getStatic() {
    return this.queryBus.execute(new GetStaticContentQuery());
  }

  @Get('dynamic')
  @ApiOperation({ summary: 'Get dynamic public site content (team, projects, events)' })
  @ApiAutoResponse(PublicSiteDynamicContentResponseDto)
  @RequirePermissions('read:public_content')
  getDynamic(): Promise<PublicSiteDynamicContent> {
    return this.queryBus.execute(new GetDynamicContentQuery());
  }
}
