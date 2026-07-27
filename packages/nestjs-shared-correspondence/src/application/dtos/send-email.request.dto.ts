import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  ArrayNotEmpty,
  IsArray,
  IsEmail,
  IsNotEmpty,
  IsOptional,
  IsString,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';

export class SendEmailAttachmentDto {
  @ApiProperty({ description: 'Attachment file name.' })
  @IsString()
  @IsNotEmpty()
  filename: string;

  @ApiProperty({ description: 'Base64-encoded attachment bytes.' })
  @IsString()
  @IsNotEmpty()
  content: string;

  @ApiPropertyOptional({ description: 'MIME type, e.g. application/pdf.' })
  @IsOptional()
  @IsString()
  contentType?: string;

  @ApiPropertyOptional({
    description: 'Content-ID for referencing the attachment inline in the HTML body.',
  })
  @IsOptional()
  @IsString()
  cid?: string;
}

export class SendEmailDto {
  @ApiProperty({ type: [String], description: 'Recipient email addresses.' })
  @IsArray()
  @ArrayNotEmpty()
  @IsEmail({}, { each: true })
  to: string[];

  @ApiPropertyOptional({ type: [String], description: 'CC email addresses.' })
  @IsOptional()
  @IsArray()
  @IsEmail({}, { each: true })
  cc?: string[];

  @ApiPropertyOptional({ type: [String], description: 'BCC email addresses.' })
  @IsOptional()
  @IsArray()
  @IsEmail({}, { each: true })
  bcc?: string[];

  @ApiProperty({ description: 'Email subject line.' })
  @IsString()
  @IsNotEmpty()
  subject: string;

  @ApiProperty({ description: 'Fully-resolved HTML body.' })
  @IsString()
  @IsNotEmpty()
  html: string;

  @ApiPropertyOptional({ description: 'Optional plain-text alternative body.' })
  @IsOptional()
  @IsString()
  text?: string;

  @ApiPropertyOptional({
    description: 'Optional from override. Defaults to the configured sender address.',
  })
  @IsOptional()
  @IsEmail()
  from?: string;

  @ApiPropertyOptional({ type: [SendEmailAttachmentDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SendEmailAttachmentDto)
  attachments?: SendEmailAttachmentDto[];
}

export class SendEmailResultDto {
  @ApiProperty({ description: 'Whether the email was accepted and sent.' })
  accepted: boolean;
}
