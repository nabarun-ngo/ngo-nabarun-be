import {
  IsOptional,
  IsEnum,
  IsNumber,
  IsDate,
  Min,
} from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { UserStatus } from '../../domain/enums/user-status.enum';

export class UpdateUserAdminDto {
  @ApiPropertyOptional({ enum: UserStatus, example: UserStatus.ACTIVE })
  @IsOptional()
  @IsEnum(UserStatus)
  status?: UserStatus;

  @ApiPropertyOptional({ example: 500 })
  @IsOptional()
  @IsNumber()
  @Min(0)
  donationAmount?: number;

  @ApiPropertyOptional({ example: '2026-03-14T09:30:00.000Z' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  donationPauseStart?: Date;

  @ApiPropertyOptional({ example: '2026-06-14T09:30:00.000Z' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  donationPauseEnd?: Date;
}
