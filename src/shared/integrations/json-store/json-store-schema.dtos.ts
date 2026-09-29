import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsOptional, IsString } from 'class-validator';

export class GetJsonStoreSchemaQueryDto {
  @ApiProperty({ description: 'JSON store namespace', example: 'finance-reference-data' })
  @IsString()
  @IsNotEmpty()
  namespace: string;

  @ApiPropertyOptional({
    description: 'Document key — used to resolve namespace:key overrides',
    example: 'donation-status',
  })
  @IsString()
  @IsOptional()
  key?: string;
}

export class JsonStoreSchemaResolveDto {
  @ApiProperty({ example: 'finance-reference-data' })
  namespace: string;

  @ApiPropertyOptional({ example: 'donation-status' })
  key?: string;

  @ApiProperty({
    enum: ['exact', 'namespace', 'none'],
    description: 'Which registry entry matched',
  })
  match: 'exact' | 'namespace' | 'none';

  @ApiPropertyOptional({
    description: 'JSON Schema (Draft 2020-12) for the resolved Zod schema, or null when none',
    nullable: true,
    type: 'object',
    additionalProperties: true,
  })
  jsonSchema: Record<string, unknown> | null;
}

export class JsonStoreSchemaCatalogItemDto {
  @ApiProperty({ example: 'links:policies' })
  registryKey: string;

  @ApiProperty({ example: 'links' })
  namespace: string;

  @ApiPropertyOptional({ example: 'policies' })
  key?: string;

  @ApiProperty({ enum: ['reference', 'content', 'managed'] })
  group: 'reference' | 'content' | 'managed';

  @ApiProperty({ example: 'Policies' })
  label: string;

  @ApiPropertyOptional({ example: '/secured/admin/cron-jobs' })
  managedLink?: string;

  @ApiPropertyOptional({ example: 'Donation status dropdowns' })
  consumerHint?: string;

  @ApiProperty({ description: 'Whether a Zod schema is registered for this key' })
  hasSchema: boolean;
}
