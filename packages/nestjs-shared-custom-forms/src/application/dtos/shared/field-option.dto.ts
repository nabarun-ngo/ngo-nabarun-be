import { ApiProperty } from '@nestjs/swagger';

/** Canonical field option shape for OpenAPI (request and response). */
export class FieldOptionDto {
  @ApiProperty()
  key: string;

  @ApiProperty()
  label: string;
}
