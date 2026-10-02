import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, MaxLength } from 'class-validator';

export class IssueIdentityCardDto {
  @ApiPropertyOptional({
    description: 'Optional data URI of the member photo for this print only. Not stored.',
  })
  @IsOptional()
  @IsString()
  @MaxLength(2_500_000)
  pictureDataUrl?: string;
}
