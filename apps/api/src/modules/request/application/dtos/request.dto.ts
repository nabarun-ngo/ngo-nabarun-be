import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsIn,
  IsNotEmpty,
  IsObject,
  IsOptional,
  IsString,
} from 'class-validator';
import { PaginatedQueryDto } from '@nabarun-ngo/nestjs-shared-core';

export class PartyCriteriaDto {
  @ApiProperty({ type: [String], example: ['SECRETARY'] })
  roles: string[];

  @ApiProperty({ type: [String], example: ['create:users'] })
  permissions: string[];

  @ApiProperty({ type: [String], example: [] })
  groups: string[];
}

export class RequestTypeDto {
  @ApiProperty({ example: 'CONTACT_REQUEST' })
  id: string;

  @ApiProperty({ example: 1 })
  version: number;

  @ApiProperty({ example: 'Contact & Support' })
  name: string;

  @ApiPropertyOptional()
  description?: string;

  @ApiProperty({ example: 'CONTACT_REQUEST:request' })
  formKey: string;

  @ApiProperty()
  executorInstructions: string;

  @ApiProperty()
  needApproval: boolean;

  @ApiProperty({ type: PartyCriteriaDto })
  approvers: PartyCriteriaDto;

  @ApiProperty({ type: PartyCriteriaDto })
  executors: PartyCriteriaDto;
}

export class RequestStartFormFieldDto {
  @ApiProperty({ example: 'f-subject' })
  id: string;

  @ApiProperty({ example: 'subject' })
  key: string;

  @ApiProperty({ example: 'Subject' })
  label: string;

  @ApiProperty({
    description: 'Custom form field type, e.g. text, textarea, select, number, date',
    example: 'text',
  })
  fieldType: string;

  @ApiProperty({ example: true })
  mandatory: boolean;

  @ApiProperty({ type: 'array', items: { type: 'object' }, example: [] })
  fieldOptions: Array<{ key: string; label: string }>;

  @ApiProperty({ example: false })
  isHidden: boolean;

  @ApiProperty({ example: false })
  isEncrypted: boolean;

  @ApiProperty({ example: true })
  enabled: boolean;

  @ApiProperty({ example: 1 })
  sortOrder: number;

  @ApiPropertyOptional({ nullable: true, example: null })
  stepId?: string | null;

  @ApiPropertyOptional({ nullable: true, example: null })
  stepName?: string | null;

  @ApiPropertyOptional({ nullable: true, example: null })
  condition?: unknown;

  @ApiPropertyOptional({ nullable: true, example: null })
  dependentOptions?: unknown;

  @ApiPropertyOptional({ nullable: true, example: null })
  validationRules?: unknown;
}

export class RequestStartFormDto {
  @ApiProperty({ example: 'CONTACT_REQUEST' })
  type: string;

  @ApiPropertyOptional({ nullable: true, example: 'CONTACT_REQUEST:request' })
  formKey: string | null;

  @ApiPropertyOptional({ nullable: true, example: 'contact-request-form' })
  formId: string | null;

  @ApiPropertyOptional({ nullable: true, example: 'Contact & Support' })
  label: string | null;

  @ApiPropertyOptional({
    nullable: true,
    example: 'Tell us how we can help and we will get back to you.',
  })
  description: string | null;

  @ApiProperty({ type: [RequestStartFormFieldDto] })
  fields: RequestStartFormFieldDto[];
}

export class RequestPersonDto {
  @ApiProperty()
  id: string;

  @ApiPropertyOptional({ nullable: true })
  firstName?: string | null;

  @ApiPropertyOptional({ nullable: true })
  lastName?: string | null;
}

export class RequestEventDto {
  @ApiProperty()
  id: string;

  @ApiProperty()
  requestId: string;

  @ApiProperty()
  type: string;

  @ApiPropertyOptional({ nullable: true })
  actorId?: string | null;

  @ApiProperty({ type: 'object', additionalProperties: true })
  payload: Record<string, unknown>;

  @ApiProperty()
  occurredAt: Date;
}

export class RequestDto {
  @ApiProperty()
  id: string;

  @ApiProperty()
  type: string;

  @ApiProperty()
  name: string;

  @ApiProperty()
  formKey: string;

  @ApiPropertyOptional({ nullable: true })
  formSubmissionId?: string | null;

  @ApiProperty({ example: 'YetToStart' })
  status: string;

  @ApiPropertyOptional({ nullable: true })
  initiatedById?: string | null;

  @ApiPropertyOptional({ nullable: true })
  initiatedForId?: string | null;

  @ApiPropertyOptional({ nullable: true })
  assigneeId?: string | null;

  @ApiPropertyOptional({ nullable: true })
  claimedById?: string | null;

  @ApiPropertyOptional({ nullable: true })
  claimedAt?: Date | null;

  @ApiProperty({ type: [String] })
  executorRoles: string[];

  @ApiProperty({ type: [String] })
  executorGroups: string[];

  @ApiProperty({ type: [String] })
  executorPermissions: string[];

  @ApiProperty()
  needApproval: boolean;

  @ApiPropertyOptional({
    description: 'Live brief from the current request definition',
    nullable: true,
  })
  executorInstructions?: string | null;

  @ApiPropertyOptional({ type: [String] })
  approverRoles?: string[];

  @ApiPropertyOptional({ type: [String] })
  approverGroups?: string[];

  @ApiPropertyOptional({ type: [String] })
  approverPermissions?: string[];

  @ApiPropertyOptional({
    description:
      'True when Pending for Approval and the current actor is the assignee (inbox priority)',
  })
  assignedToMeAtApproval?: boolean;

  @ApiPropertyOptional({ nullable: true })
  completedAt?: Date | null;

  @ApiPropertyOptional({ nullable: true })
  decisionNote?: string | null;

  @ApiProperty()
  createdAt: Date;

  @ApiProperty()
  updatedAt: Date;

  @ApiPropertyOptional({ type: RequestPersonDto, nullable: true })
  initiatedBy?: RequestPersonDto | null;

  @ApiPropertyOptional({ type: RequestPersonDto, nullable: true })
  initiatedFor?: RequestPersonDto | null;

  @ApiPropertyOptional({ type: RequestPersonDto, nullable: true })
  assignee?: RequestPersonDto | null;

  @ApiPropertyOptional({ type: RequestPersonDto, nullable: true })
  claimedBy?: RequestPersonDto | null;

  @ApiPropertyOptional({ type: [RequestEventDto] })
  events?: RequestEventDto[];
}

export class CreateRequestDto {
  @ApiProperty({ example: 'CONTACT_REQUEST' })
  @IsString()
  @IsNotEmpty()
  type: string;

  @ApiPropertyOptional({ description: 'Target member when creating on behalf of another' })
  @IsOptional()
  @IsString()
  initiatedForId?: string;

  @ApiProperty({
    description: 'Start-form field values keyed by field key',
    type: 'object',
    additionalProperties: true,
  })
  @IsObject()
  formValues: Record<string, unknown>;
}

export class ListRequestsQueryDto extends PaginatedQueryDto {
  @ApiProperty({ enum: ['mine', 'inbox', 'started'], example: 'mine' })
  @IsIn(['mine', 'inbox', 'started'])
  scope: 'mine' | 'inbox' | 'started';

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  type?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  status?: string;

  @ApiPropertyOptional({ description: 'Partial request id match' })
  @IsOptional()
  @IsString()
  id?: string;
}

export class AssignRequestDto {
  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  assigneeId: string;
}

export class DecisionNoteDto {
  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  note?: string;
}

export class WithdrawRequestDto {
  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  note?: string;
}
