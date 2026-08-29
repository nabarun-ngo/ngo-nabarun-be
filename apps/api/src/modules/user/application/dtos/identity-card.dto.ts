import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class IdentityCardPdfResult {
  buffer!: Buffer;
  fileName!: string;
  uniqueMemberId!: string;
}

export enum IdentityCardVerificationOutcome {
  VALID = 'VALID',
  INVALID = 'INVALID',
  UNKNOWN = 'UNKNOWN',
}

export class IdentityCardVerificationDto {
  @ApiProperty({ enum: IdentityCardVerificationOutcome, example: IdentityCardVerificationOutcome.VALID })
  outcome!: IdentityCardVerificationOutcome;

  @ApiPropertyOptional({ example: 'NM24128765' })
  uniqueMemberId?: string;

  @ApiPropertyOptional({ example: 'Asha Verma' })
  displayName?: string;
}
