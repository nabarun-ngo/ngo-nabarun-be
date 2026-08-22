import { Controller, Get, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { UnifiedAuthGuard, RequirePermissions, PermissionsGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoResponse } from '@nabarun-ngo/nestjs-shared-core';
import {
  GetJsonStoreSchemaQueryDto,
  JsonStoreSchemaCatalogItemDto,
  JsonStoreSchemaResolveDto,
} from './json-store-schema.dtos';
import { JsonStoreSchemaService } from './json-store-schema.service';

@ApiTags('JSON Store')
@ApiBearerAuth('jwt')
@UseGuards(UnifiedAuthGuard, PermissionsGuard)
@Controller('json-store')
export class JsonStoreSchemaController {
  constructor(private readonly schemas: JsonStoreSchemaService) {}

  @Get('schemas')
  @RequirePermissions('read:json_documents')
  @ApiOperation({ summary: 'List registered JSON store schema catalog entries' })
  @ApiAutoResponse(JsonStoreSchemaCatalogItemDto, { isArray: true })
  listSchemas(): JsonStoreSchemaCatalogItemDto[] {
    return this.schemas.listCatalog();
  }

  @Get('schema')
  @RequirePermissions('read:json_documents')
  @ApiOperation({
    summary: 'Resolve JSON Schema for a namespace (and optional key)',
    description:
      'Resolves namespace:key first, then namespace. Returns match=none and jsonSchema=null when unregistered.',
  })
  @ApiAutoResponse(JsonStoreSchemaResolveDto)
  getSchema(@Query() query: GetJsonStoreSchemaQueryDto): JsonStoreSchemaResolveDto {
    return this.schemas.resolve(query.namespace, query.key);
  }
}
