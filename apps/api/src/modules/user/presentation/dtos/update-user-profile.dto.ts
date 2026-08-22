import {
  IsString,
  IsOptional,
  IsBoolean,
  IsDateString,
  ValidateNested,
  IsArray,
} from 'class-validator';
import { ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';

export class PhoneNumberInputDto {
  @ApiPropertyOptional({ example: '+91' })
  @IsString()
  phoneCode!: string;

  @ApiPropertyOptional({ example: '9876543210' })
  @IsString()
  phoneNumber!: string;

  @ApiPropertyOptional({ example: false })
  @IsOptional()
  @IsBoolean()
  hidden?: boolean;
}

export class AddressInputDto {
  @ApiPropertyOptional({ example: '12 Gandhi Road' })
  @IsString()
  addressLine1!: string;

  @ApiPropertyOptional({ example: 'Near Kali Temple' })
  @IsOptional()
  @IsString()
  addressLine2?: string;

  @ApiPropertyOptional({ example: 'Ward 14' })
  @IsOptional()
  @IsString()
  addressLine3?: string;

  @ApiPropertyOptional({ example: 'Barasat' })
  @IsString()
  hometown!: string;

  @ApiPropertyOptional({ example: '700124' })
  @IsString()
  zipCode!: string;

  @ApiPropertyOptional({ example: 'West Bengal' })
  @IsString()
  state!: string;

  @ApiPropertyOptional({ example: 'North 24 Parganas' })
  @IsString()
  district!: string;

  @ApiPropertyOptional({ example: 'India' })
  @IsString()
  country!: string;
}

export class SocialLinkInputDto {
  @ApiPropertyOptional({ example: 'LinkedIn' })
  @IsString()
  linkName!: string;

  @ApiPropertyOptional({ example: 'LINKEDIN' })
  @IsString()
  linkType!: string;

  @ApiPropertyOptional({ example: 'https://linkedin.com/in/ashaverma' })
  @IsString()
  linkValue!: string;
}

export class UpdateUserProfileDto {
  @ApiPropertyOptional({ example: 'Ms' })
  @IsOptional()
  @IsString()
  title?: string;

  @ApiPropertyOptional({ example: 'Asha' })
  @IsOptional()
  @IsString()
  firstName?: string;

  @ApiPropertyOptional({ example: 'Rani' })
  @IsOptional()
  @IsString()
  middleName?: string;

  @ApiPropertyOptional({ example: 'Verma' })
  @IsOptional()
  @IsString()
  lastName?: string;

  @ApiPropertyOptional({ example: '1994-03-14' })
  @IsOptional()
  @IsDateString()
  dateOfBirth?: string;

  @ApiPropertyOptional({ example: 'FEMALE' })
  @IsOptional()
  @IsString()
  gender?: string;

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

  @ApiPropertyOptional({ example: false })
  @IsOptional()
  @IsBoolean()
  isSameAddress?: boolean;

  @ApiPropertyOptional()
  @IsOptional()
  @ValidateNested()
  @Type(() => PhoneNumberInputDto)
  primaryPhone?: PhoneNumberInputDto;

  @ApiPropertyOptional()
  @IsOptional()
  @ValidateNested()
  @Type(() => PhoneNumberInputDto)
  secondaryPhone?: PhoneNumberInputDto;

  @ApiPropertyOptional()
  @IsOptional()
  @ValidateNested()
  @Type(() => AddressInputDto)
  presentAddress?: AddressInputDto;

  @ApiPropertyOptional()
  @IsOptional()
  @ValidateNested()
  @Type(() => AddressInputDto)
  permanentAddress?: AddressInputDto;

  @ApiPropertyOptional({ type: [SocialLinkInputDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SocialLinkInputDto)
  socialMediaLinks?: SocialLinkInputDto[];
}
