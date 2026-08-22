import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsNotEmpty, IsObject, IsOptional, IsString } from 'class-validator';

export class SaveFormDraftDto {
  @ApiProperty({ example: 'PROJECT' })
  @IsString()
  @IsNotEmpty()
  entityType: string;

  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  @IsString()
  @IsNotEmpty()
  entityId: string;

  @ApiProperty({
    type: Object,
    description: 'Map of field key → raw user-provided value',
    example: { full_name: 'Asha Verma', emergency_contact: '+919876543210' },
  })
  @IsObject()
  values: Record<string, unknown>;
}

export class SubmitFormDto {
  @ApiProperty({ example: 'PROJECT' })
  @IsString()
  @IsNotEmpty()
  entityType: string;

  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  @IsString()
  @IsNotEmpty()
  entityId: string;

  @ApiPropertyOptional({
    type: Object,
    description: 'Optional final values to persist before submit',
    example: { full_name: 'Asha Verma', emergency_contact: '+919876543210' },
  })
  @IsObject()
  @IsOptional()
  values?: Record<string, unknown>;
}

export class ClearFormSubmissionDto {
  @ApiProperty({ example: 'PROJECT' })
  @IsString()
  @IsNotEmpty()
  entityType: string;

  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  @IsString()
  @IsNotEmpty()
  entityId: string;
}
