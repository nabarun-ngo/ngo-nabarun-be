import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsBoolean, IsEnum, IsNotEmpty, IsOptional, IsString, ValidateNested } from 'class-validator';
import { PaginatedQueryDto } from '@nabarun-ngo/nestjs-shared-core';
import { MeetingType } from '../../domain/enums/meeting-type.enum';

export class MeetingParticipantDto {
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) @IsOptional() @IsString() id?: string;
  @ApiPropertyOptional({ example: 'Asha Verma' }) @IsOptional() @IsString() name?: string;
  @ApiProperty({ example: 'asha.verma@example.org' }) @IsNotEmpty() @IsString() email!: string;
  @ApiPropertyOptional({ example: 'accepted' }) @IsOptional() @IsString() attended?: string;
}

export class MeetingAgendaItemDto {
  @ApiProperty({ example: 'Review volunteer enrolment progress' }) @IsNotEmpty() @IsString() agenda!: string;
  @ApiPropertyOptional({ example: 'Team reviewed enrolment progress and agreed next steps.' }) @IsOptional() @IsString() outcomes?: string;
}

export class CreateMeetingDto {
  @ApiProperty({ example: 'Monthly programme review' }) @IsNotEmpty() @IsString() summary!: string;
  @ApiProperty({ enum: MeetingType, example: MeetingType.ONLINE }) @IsNotEmpty() @IsEnum(MeetingType) type!: MeetingType;
  @ApiPropertyOptional({ example: 'Monthly review of programme delivery with the volunteer coordinators.' }) @IsOptional() @IsString() description?: string;
  @ApiPropertyOptional({ type: [MeetingAgendaItemDto] })
  @IsOptional()
  @ValidateNested({ each: true })
  @Type(() => MeetingAgendaItemDto)
  agenda?: MeetingAgendaItemDto[];

  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) @IsNotEmpty() @IsString() startTime!: string;
  @ApiProperty({ example: '2026-03-14T10:15:00.000Z' }) @IsNotEmpty() @IsString() endTime!: string;

  @ApiProperty({ type: [MeetingParticipantDto] })
  @ValidateNested({ each: true })
  @Type(() => MeetingParticipantDto)
  attendees!: MeetingParticipantDto[];

  @ApiPropertyOptional({ example: 'Nabarun office, 12 Gandhi Road, Barasat' }) @IsOptional() @IsString() location?: string;
}

export class UpdateMeetingDto {
  @ApiPropertyOptional({ example: 'Monthly programme review' }) @IsOptional() @IsString() summary?: string;
  @ApiPropertyOptional({ example: 'Monthly review of programme delivery with the volunteer coordinators.' }) @IsOptional() @IsString() description?: string;

  @ApiPropertyOptional({ type: [MeetingAgendaItemDto] })
  @IsOptional()
  @ValidateNested({ each: true })
  @Type(() => MeetingAgendaItemDto)
  agenda?: MeetingAgendaItemDto[];

  @ApiPropertyOptional({ example: 'Team reviewed enrolment progress and agreed next steps.' }) @IsOptional() @IsString() outcomes?: string;
  @ApiPropertyOptional({ example: '2026-03-14T09:30:00.000Z' }) @IsOptional() @IsString() startTime?: string;
  @ApiPropertyOptional({ example: '2026-03-14T10:15:00.000Z' }) @IsOptional() @IsString() endTime?: string;

  @ApiPropertyOptional({ type: [MeetingParticipantDto] })
  @IsOptional()
  @ValidateNested({ each: true })
  @Type(() => MeetingParticipantDto)
  attendees?: MeetingParticipantDto[];

  @ApiPropertyOptional({ example: 'Nabarun office, 12 Gandhi Road, Barasat' }) @IsOptional() @IsString() location?: string;

  @ApiPropertyOptional({ default: false, example: false })
  @IsOptional()
  @IsBoolean()
  cancelEvent?: boolean = false;
}

export class MeetingDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' }) id!: string;
  @ApiProperty({ enum: MeetingType, example: MeetingType.ONLINE }) type!: MeetingType;
  @ApiProperty({ example: 'Monthly programme review' }) summary!: string;
  @ApiPropertyOptional({ example: 'Monthly review of programme delivery with the volunteer coordinators.' }) description?: string;
  @ApiPropertyOptional({ type: [MeetingAgendaItemDto] }) agenda?: MeetingAgendaItemDto[];
  @ApiPropertyOptional({ example: 'Team reviewed enrolment progress and agreed next steps.' }) outcomes?: string;
  @ApiPropertyOptional({ example: 'Nabarun office, 12 Gandhi Road, Barasat' }) location?: string;
  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) startTime!: Date;
  @ApiProperty({ example: '2026-03-14T10:15:00.000Z' }) endTime!: Date;
  @ApiPropertyOptional({ type: [MeetingParticipantDto] }) attendees?: MeetingParticipantDto[];
  @ApiPropertyOptional({ example: 'https://meet.google.com/abc-defg-hij' }) meetLink?: string;
  @ApiPropertyOptional({ example: 'https://calendar.google.com/calendar/event?eid=3f8a1c925d47' }) calendarLink?: string;
  @ApiProperty({ example: 'confirmed' }) status!: string;
  @ApiPropertyOptional({ example: 'asha.verma@example.org' }) hostEmail?: string;
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) createdById?: string;
  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) createdAt!: Date;
  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' }) updatedAt!: Date;
}

export class MeetingDetailFilterDto extends PaginatedQueryDto {
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' }) @IsOptional() @IsString() createdById?: string;
  @ApiPropertyOptional({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' }) @IsOptional() @IsString() participantId?: string;
  @ApiPropertyOptional({ example: 'asha.verma@example.org' }) @IsOptional() @IsString() participantEmail?: string;
}
