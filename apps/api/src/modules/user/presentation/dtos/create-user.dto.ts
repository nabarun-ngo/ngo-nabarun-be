import {
  IsEmail,
  IsString,
  IsNotEmpty,
  IsOptional,
  IsBoolean,
  IsDateString,
  MaxLength,
} from 'class-validator';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';

export class CreateUserDto {
  @ApiProperty({ example: 'asha.verma@example.org' })
  @IsEmail()
  email!: string;

  @ApiProperty({ example: 'Asha' })
  @IsString()
  @IsNotEmpty()
  firstName!: string;

  @ApiProperty({ example: 'Verma' })
  @IsString()
  @IsNotEmpty()
  lastName!: string;

  @ApiPropertyOptional({ example: 'Ms' })
  @IsOptional()
  @IsString()
  title?: string;

  @ApiPropertyOptional({ example: 'Rani' })
  @IsOptional()
  @IsString()
  middleName?: string;

  @ApiPropertyOptional({ example: '1994-03-14' })
  @IsOptional()
  @IsDateString()
  dateOfBirth?: string;

  @ApiPropertyOptional({ example: 'FEMALE' })
  @IsOptional()
  @IsString()
  gender?: string;

  @ApiPropertyOptional({ example: 'B+', description: 'ABO/Rh blood group, e.g. A+, O-' })
  @IsOptional()
  @IsString()
  @MaxLength(8)
  bloodGroup?: string;

  @ApiPropertyOptional({ example: 'Volunteer coordinator for the Barasat education programme.' })
  @IsOptional()
  @IsString()
  about?: string;

  @ApiPropertyOptional({ example: 'https://cdn.nabarun.org/avatars/asha-verma.jpg' })
  @IsOptional()
  @IsString()
  picture?: string;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @IsBoolean()
  isPublic?: boolean;
}
