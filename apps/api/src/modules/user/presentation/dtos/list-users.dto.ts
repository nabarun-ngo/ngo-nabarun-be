import { IsOptional, IsString, IsEnum, IsBoolean, IsIn } from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { PaginatedQueryDto } from '@nabarun-ngo/nestjs-shared-core';
import { UserStatus } from '../../domain/enums/user-status.enum';

const USER_SORT_FIELDS = ['firstName', 'lastName', 'email', 'status', 'createdAt', 'updatedAt'] as const;

export class ListUsersQueryDto extends PaginatedQueryDto {
  @ApiPropertyOptional({ example: 'Asha' })
  @IsOptional()
  @IsString()
  firstName?: string;

  @ApiPropertyOptional({ example: 'Verma' })
  @IsOptional()
  @IsString()
  lastName?: string;

  @ApiPropertyOptional({ example: 'asha.verma@example.org' })
  @IsOptional()
  @IsString()
  email?: string;

  @ApiPropertyOptional({ enum: UserStatus, example: UserStatus.ACTIVE })
  @IsOptional()
  @IsEnum(UserStatus)
  status?: UserStatus;

  @ApiPropertyOptional({ example: '+919876543210' })
  @IsOptional()
  @IsString()
  phoneNumber?: string;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @Type(() => Boolean)
  @IsBoolean()
  isPublic?: boolean;

  @ApiPropertyOptional({ enum: USER_SORT_FIELDS, example: 'createdAt' })
  @IsOptional()
  @IsIn(USER_SORT_FIELDS)
  declare sortBy?: string;
}
