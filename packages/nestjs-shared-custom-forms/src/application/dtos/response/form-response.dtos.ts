import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { CustomFieldType } from '../../../domain/enums/custom-field-type.enum';
import { FormStatus } from '../../../domain/enums/form-status.enum';
import { FieldOptionDto } from '../shared/field-option.dto';
import { FieldConditionDto } from '../shared/field-condition.dto';
import { DependentOptionsDto } from '../shared/dependent-options.dto';
import { FieldRegexRuleDto } from '../shared/field-regex-rule.dto';
import { FieldValidationRulesDto } from '../shared/field-validation-rules.dto';

export class FieldOptionResponseDto extends FieldOptionDto {}

export class FieldConditionResponseDto extends FieldConditionDto {}

export class DependentOptionsResponseDto extends DependentOptionsDto {
  @ApiProperty({
    description: 'parentValue → available FieldOptions',
    example: { india: [{ key: 'west-bengal', label: 'West Bengal' }] },
  })
  declare optionMap: Record<string, FieldOptionResponseDto[]>;
}

export class FieldRegexRuleResponseDto extends FieldRegexRuleDto {}

export class FieldValidationRulesResponseDto extends FieldValidationRulesDto {
  @ApiProperty({
    type: [FieldRegexRuleResponseDto],
    description: 'All patterns must match (AND)',
  })
  declare patterns: FieldRegexRuleResponseDto[];
}

export class FormFieldDefinitionResponseDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  id: string;

  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  formId: string;

  @ApiProperty({ example: 'emergency_contact' })
  key: string;

  @ApiProperty({ example: 'Emergency contact number' })
  label: string;

  @ApiProperty({ enum: Object.values(CustomFieldType), example: CustomFieldType.Phone })
  fieldType: CustomFieldType;

  @ApiProperty({ example: true })
  mandatory: boolean;

  @ApiProperty({ type: [FieldOptionResponseDto] })
  fieldOptions: FieldOptionResponseDto[];

  @ApiProperty({ example: false })
  isHidden: boolean;

  @ApiProperty({ example: true })
  isEncrypted: boolean;

  @ApiProperty({ example: true })
  enabled: boolean;

  @ApiProperty({ example: 1 })
  sortOrder: number;

  @ApiPropertyOptional({ nullable: true, description: 'Wizard step identifier for multi-step forms', example: 'contact-details' })
  stepId: string | null;

  @ApiPropertyOptional({ nullable: true, description: 'Display label for the wizard step', example: 'Contact details' })
  stepName: string | null;

  @ApiPropertyOptional({ type: FieldConditionResponseDto, nullable: true })
  condition: FieldConditionResponseDto | null;

  @ApiPropertyOptional({ type: DependentOptionsResponseDto, nullable: true })
  dependentOptions: DependentOptionsResponseDto | null;

  @ApiPropertyOptional({ type: FieldValidationRulesResponseDto, nullable: true })
  validationRules: FieldValidationRulesResponseDto | null;

  @ApiProperty({ type: [String], example: ['read:custom_forms', 'admin:custom_forms'] })
  viewPermissions: string[];

  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' })
  createdAt: Date;

  @ApiPropertyOptional({ nullable: true, example: '2026-03-14T09:30:00.000Z' })
  updatedAt: Date | null;
}

export class FormResponseDto {
  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  id: string;

  @ApiProperty({ example: 'PROJECT' })
  entityType: string;

  @ApiProperty({ example: 'volunteer-intake' })
  key: string;

  @ApiProperty({ example: 'Volunteer Intake Form' })
  label: string;

  @ApiPropertyOptional({ nullable: true, example: 'Collects availability and emergency contact details from new volunteers' })
  description: string | null;

  @ApiProperty({ enum: Object.values(FormStatus), example: FormStatus.Published })
  status: FormStatus;

  @ApiProperty({ type: [String], example: ['admin:custom_forms'] })
  managePermissions: string[];

  @ApiProperty({ type: [String], example: ['read:custom_forms'] })
  readPermissions: string[];

  @ApiProperty({ type: [String], example: ['write:custom_forms'] })
  writePermissions: string[];

  @ApiPropertyOptional({ type: [FormFieldDefinitionResponseDto] })
  fields?: FormFieldDefinitionResponseDto[];

  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' })
  createdAt: Date;

  @ApiPropertyOptional({ nullable: true, example: '2026-03-14T09:30:00.000Z' })
  updatedAt: Date | null;

  @ApiPropertyOptional({ nullable: true, example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  createdBy?: string;

  @ApiPropertyOptional({ nullable: true, example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  publishedBy?: string;

  @ApiPropertyOptional({ nullable: true, example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  disabledBy?: string;
}

export class ResolvedFormFieldValueResponseDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  fieldDefId: string;

  @ApiProperty({ example: 'emergency_contact' })
  key: string;

  @ApiProperty({ example: 'Emergency contact number' })
  label: string;

  @ApiProperty({ enum: Object.values(CustomFieldType), example: CustomFieldType.Phone })
  fieldType: CustomFieldType;

  @ApiPropertyOptional({ type: Object, nullable: true, example: '+919876543210' })
  value: unknown;

  @ApiProperty({ type: [FieldOptionResponseDto] })
  availableOptions: FieldOptionResponseDto[];

  @ApiPropertyOptional({ type: DependentOptionsResponseDto, nullable: true })
  dependentOptions: DependentOptionsResponseDto | null;

  @ApiProperty({ example: true })
  mandatory: boolean;

  @ApiPropertyOptional({ type: FieldValidationRulesResponseDto, nullable: true })
  validationRules: FieldValidationRulesResponseDto | null;

  @ApiProperty({ example: true })
  isEncrypted: boolean;

  @ApiProperty({ example: false })
  isHidden: boolean;

  @ApiPropertyOptional({ type: FieldConditionResponseDto, nullable: true })
  condition: FieldConditionResponseDto | null;
}

export class FormFieldValueHistoryEntryResponseDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  id: string;

  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  fieldDefId: string;

  @ApiProperty({ example: 'emergency_contact' })
  fieldKey: string;

  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  formId: string;

  @ApiProperty({ example: 'PROJECT' })
  entityType: string;

  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  entityId: string;

  @ApiPropertyOptional({ type: Object, nullable: true, example: '+919812345678' })
  oldValue: unknown;

  @ApiPropertyOptional({ type: Object, nullable: true, example: '+919876543210' })
  newValue: unknown;

  @ApiProperty({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  changedBy: string;

  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' })
  changedAt: Date;
}
