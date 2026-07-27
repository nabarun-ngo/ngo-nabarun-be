import { ApiProperty } from '@nestjs/swagger';

export class FormValidationResultResponseDto {
  @ApiProperty()
  valid: boolean;

  @ApiProperty({ type: [String] })
  missingMandatory: string[];

  @ApiProperty({ type: [String] })
  conditionViolations: string[];

  @ApiProperty({ type: [String] })
  validationViolations: string[];
}
