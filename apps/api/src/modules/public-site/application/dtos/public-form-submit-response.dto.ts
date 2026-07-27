import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class PublicFormSubmitResponseDto {
  @ApiProperty({ example: 'Request submitted successfully' })
  message!: string;

  @ApiPropertyOptional({ description: 'Workflow instance or form submission reference' })
  referenceId?: string;
}
