import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class FieldRegexRuleDto {
  @ApiProperty({ description: 'JavaScript regex source (no delimiters)' })
  pattern: string;

  @ApiPropertyOptional({ description: 'Custom error message when the value does not match this pattern' })
  regexErrMsg?: string;
}
