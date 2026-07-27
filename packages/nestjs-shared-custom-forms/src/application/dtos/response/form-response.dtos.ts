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
  @ApiProperty({ description: 'parentValue → available FieldOptions' })
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
  @ApiProperty()
  id: string;

  @ApiProperty()
  formId: string;

  @ApiProperty()
  key: string;

  @ApiProperty()
  label: string;

  @ApiProperty({ enum: Object.values(CustomFieldType) })
  fieldType: CustomFieldType;

  @ApiProperty()
  mandatory: boolean;

  @ApiProperty({ type: [FieldOptionResponseDto] })
  fieldOptions: FieldOptionResponseDto[];

  @ApiProperty()
  isHidden: boolean;

  @ApiProperty()
  isEncrypted: boolean;

  @ApiProperty()
  enabled: boolean;

  @ApiProperty()
  sortOrder: number;

  @ApiPropertyOptional({ nullable: true, description: 'Wizard step identifier for multi-step forms' })
  stepId: string | null;

  @ApiPropertyOptional({ nullable: true, description: 'Display label for the wizard step' })
  stepName: string | null;

  @ApiPropertyOptional({ type: FieldConditionResponseDto, nullable: true })
  condition: FieldConditionResponseDto | null;

  @ApiPropertyOptional({ type: DependentOptionsResponseDto, nullable: true })
  dependentOptions: DependentOptionsResponseDto | null;

  @ApiPropertyOptional({ type: FieldValidationRulesResponseDto, nullable: true })
  validationRules: FieldValidationRulesResponseDto | null;

  @ApiProperty({ type: [String] })
  viewPermissions: string[];

  @ApiProperty()
  createdAt: Date;

  @ApiPropertyOptional({ nullable: true })
  updatedAt: Date | null;
}

export class FormResponseDto {
  @ApiProperty()
  id: string;

  @ApiProperty()
  entityType: string;

  @ApiProperty()
  key: string;

  @ApiProperty()
  label: string;

  @ApiPropertyOptional({ nullable: true })
  description: string | null;

  @ApiProperty({ enum: Object.values(FormStatus) })
  status: FormStatus;

  @ApiProperty({ type: [String] })
  managePermissions: string[];

  @ApiProperty({ type: [String] })
  readPermissions: string[];

  @ApiProperty({ type: [String] })
  writePermissions: string[];

  @ApiPropertyOptional({ type: [FormFieldDefinitionResponseDto] })
  fields?: FormFieldDefinitionResponseDto[];

  @ApiProperty()
  createdAt: Date;

  @ApiPropertyOptional({ nullable: true })
  updatedAt: Date | null;

  @ApiPropertyOptional({ nullable: true })
  createdBy?: string;

  @ApiPropertyOptional({ nullable: true })
  publishedBy?: string;

  @ApiPropertyOptional({ nullable: true })
  disabledBy?: string;
}

export class ResolvedFormFieldValueResponseDto {
  @ApiProperty()
  fieldDefId: string;

  @ApiProperty()
  key: string;

  @ApiProperty()
  label: string;

  @ApiProperty({ enum: Object.values(CustomFieldType) })
  fieldType: CustomFieldType;

  @ApiPropertyOptional({ type: Object, nullable: true })
  value: unknown;

  @ApiProperty({ type: [FieldOptionResponseDto] })
  availableOptions: FieldOptionResponseDto[];

  @ApiPropertyOptional({ type: DependentOptionsResponseDto, nullable: true })
  dependentOptions: DependentOptionsResponseDto | null;

  @ApiProperty()
  mandatory: boolean;

  @ApiPropertyOptional({ type: FieldValidationRulesResponseDto, nullable: true })
  validationRules: FieldValidationRulesResponseDto | null;

  @ApiProperty()
  isEncrypted: boolean;

  @ApiProperty()
  isHidden: boolean;

  @ApiPropertyOptional({ type: FieldConditionResponseDto, nullable: true })
  condition: FieldConditionResponseDto | null;
}

export class FormFieldValueHistoryEntryResponseDto {
  @ApiProperty()
  id: string;

  @ApiProperty()
  fieldDefId: string;

  @ApiProperty()
  fieldKey: string;

  @ApiProperty()
  formId: string;

  @ApiProperty()
  entityType: string;

  @ApiProperty()
  entityId: string;

  @ApiPropertyOptional({ type: Object, nullable: true })
  oldValue: unknown;

  @ApiPropertyOptional({ type: Object, nullable: true })
  newValue: unknown;

  @ApiProperty()
  changedBy: string;

  @ApiProperty()
  changedAt: Date;
}
