import { IsNotEmpty, IsOptional, IsString, IsUrl, MinLength } from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class InitiatePasswordChangeDto {
  @ApiProperty({
    description: 'Current password for the signed-in user (TLS-protected in transit).',
    example: 'CurrentP@ssw0rd!',
    writeOnly: true,
  })
  @IsString()
  @IsNotEmpty()
  @MinLength(1)
  currentPassword!: string;

  @ApiPropertyOptional({
    description:
      'Where the user should return after completing the hosted password change. ' +
      'Must be within the configured application origin; otherwise the default app URL is used.',
    example: 'https://app.example.com/dashboard',
  })
  @IsOptional()
  @IsString()
  @IsUrl({ require_tld: false })
  redirectUrl?: string;
}
