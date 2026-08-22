import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { UserStatus } from '../../domain/enums/user-status.enum';

export interface PhoneNumberDto {
  phoneCode: string;
  phoneNumber: string;
  hidden: boolean;
  isPrimary: boolean;
}

export interface AddressDto {
  addressType: string;
  addressLine1: string;
  addressLine2?: string;
  addressLine3?: string;
  hometown: string;
  zipCode: string;
  state: string;
  district: string;
  country: string;
}

export interface SocialLinkDto {
  id: string;
  linkName: string;
  linkType: string;
  linkValue: string;
}

export class UserResponseDto {
  @ApiProperty({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  id!: string;

  @ApiProperty({ example: 'asha.verma@example.org' })
  email!: string;

  @ApiPropertyOptional({ example: 'auth0|65f1a2b3c4d5e6f708192a3b' })
  idpSub?: string;

  @ApiProperty({ example: 'Asha' })
  firstName!: string;

  @ApiProperty({ example: 'Verma' })
  lastName!: string;

  @ApiProperty({ example: 'Asha Verma' })
  fullName!: string;

  @ApiProperty({ example: 'AV' })
  initials!: string;

  @ApiPropertyOptional({ example: 'Ms' })
  title?: string;

  @ApiPropertyOptional({ example: 'Rani' })
  middleName?: string;

  @ApiPropertyOptional({ example: '1994-03-14' })
  dateOfBirth?: Date;

  @ApiPropertyOptional({ example: 'FEMALE' })
  gender?: string;

  @ApiPropertyOptional({ example: 'Volunteer coordinator for the Barasat education programme.' })
  about?: string;

  @ApiPropertyOptional({ example: 'https://cdn.nabarun.org/avatars/asha-verma.jpg' })
  picture?: string;

  @ApiProperty({ enum: UserStatus, example: UserStatus.ACTIVE })
  status!: UserStatus;

  @ApiProperty({ example: true })
  isProfileComplete!: boolean;

  @ApiProperty({ example: true })
  isPublic!: boolean;

  @ApiPropertyOptional({ example: false })
  isSameAddress?: boolean;

  @ApiPropertyOptional()
  primaryPhone?: PhoneNumberDto;

  @ApiPropertyOptional()
  secondaryPhone?: PhoneNumberDto;

  @ApiPropertyOptional()
  presentAddress?: AddressDto;

  @ApiPropertyOptional()
  permanentAddress?: AddressDto;

  @ApiProperty()
  socialMediaLinks!: SocialLinkDto[];

  @ApiProperty({
    type: [String],
    example: ['MEMBER', 'VOLUNTEER'],
    description: 'Denormalized active Auth role keys for this member',
  })
  roleKeys!: string[];

  @ApiPropertyOptional({ example: '2026-03-14T09:30:00.000Z' })
  createdAt?: Date;

  @ApiPropertyOptional({ example: '2026-03-14T09:30:00.000Z' })
  updatedAt?: Date;

  /** Missing fields returned only by GetMyProfileHandler for the complete-profile form. */
  @ApiPropertyOptional({ example: ['dateOfBirth', 'gender'] })
  missingFields?: string[];
}

export class UserListResponseDto {
  @ApiProperty()
  items!: UserResponseDto[];

  @ApiProperty({ example: 42 })
  total!: number;

  @ApiProperty({ example: 0 })
  pageIndex!: number;

  @ApiProperty({ example: 20 })
  pageSize!: number;
}

export class UserRefDataResponseDto {
  @ApiProperty({ example: ['DRAFT', 'ACTIVE', 'BLOCKED', 'DELETED'] })
  userStatuses!: string[];

  @ApiProperty({ example: [{ key: 'MS', value: 'Ms' }] })
  userTitles!: { key: string; value: string }[];

  @ApiProperty({ example: [{ key: 'FEMALE', value: 'Female' }] })
  userGenders!: { key: string; value: string }[];

  @ApiProperty({ example: [{ key: 'AADHAAR', value: 'Aadhaar card' }] })
  documentTypes!: { key: string; value: string }[];

  @ApiProperty({ example: [{ key: 'IN', value: 'India' }] })
  countries!: { key: string; value: string }[];

  @ApiProperty({ example: [{ key: 'WB', value: 'West Bengal', countryCode: 'IN' }] })
  states!: { key: string; value: string; countryCode?: string }[];

  @ApiProperty({ example: [{ key: 'NORTH_24_PARGANAS', value: 'North 24 Parganas', stateCode: 'WB' }] })
  districts!: { key: string; value: string; stateCode?: string }[];

  @ApiProperty({ example: [{ key: 'IN', value: 'India (+91)', description: '+91' }] })
  phoneCodes!: { key: string; value: string; description?: string }[];

  @ApiProperty({ example: [{ key: 'MEMBER', value: 'Member' }] })
  availableRoles!: { key: string; value: string }[];
}
