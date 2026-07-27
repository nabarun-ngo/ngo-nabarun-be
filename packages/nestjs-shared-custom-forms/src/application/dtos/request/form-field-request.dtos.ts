import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  ArrayMinSize,
  ArrayUnique,
  IsArray,
  IsBoolean,
  IsIn,
  IsInt,
  IsNotEmpty,
  IsOptional,
  IsString,
  Min,
  ValidateNested,
} from 'class-validator';
import { Type } from 'class-transformer';
import { CustomFieldType } from '../../../domain/enums/custom-field-type.enum';
import { FieldOptionDto } from '../shared/field-option.dto';
import { FieldConditionDto } from '../shared/field-condition.dto';
import { DependentOptionsDto } from '../shared/dependent-options.dto';
import { FieldRegexRuleDto } from '../shared/field-regex-rule.dto';
import { FieldValidationRulesDto } from '../shared/field-validation-rules.dto';

/** Request body field option with validation (extends shared OpenAPI shape). */
export class FieldOptionInputDto extends FieldOptionDto {
  @IsString()
  @IsNotEmpty()
  declare key: string;

  @IsString()
  @IsNotEmpty()
  declare label: string;
}

export class FieldConditionInputDto extends FieldConditionDto {
  @IsString()
  @IsNotEmpty()
  declare dependsOnKey: string;

  @IsIn(['equals', 'not_equals', 'in', 'not_in'])
  declare operator: 'equals' | 'not_equals' | 'in' | 'not_in';
}

export class DependentOptionsInputDto extends DependentOptionsDto {
  @IsString()
  @IsNotEmpty()
  declare dependsOnKey: string;

  declare optionMap: Record<string, FieldOptionInputDto[]>;
}

export class FieldRegexRuleInputDto extends FieldRegexRuleDto {
  @IsString()
  @IsNotEmpty()
  declare pattern: string;

  @IsString()
  @IsOptional()
  declare regexErrMsg?: string;
}

export class FieldValidationRulesInputDto extends FieldValidationRulesDto {
  @IsArray()
  @ArrayMinSize(1)
  @ValidateNested({ each: true })
  @Type(() => FieldRegexRuleInputDto)
  declare patterns: FieldRegexRuleInputDto[];
}

const FIELD_TYPES = Object.values(CustomFieldType);

export class AddFormFieldDto {
  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  key: string;

  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  label: string;

  @ApiProperty({ enum: FIELD_TYPES })
  @IsIn(FIELD_TYPES)
  fieldType: CustomFieldType;

  @ApiPropertyOptional({ default: false })
  @IsBoolean()
  @IsOptional()
  mandatory?: boolean;

  @ApiPropertyOptional({ type: [FieldOptionInputDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => FieldOptionInputDto)
  @IsOptional()
  fieldOptions?: FieldOptionInputDto[];

  @ApiPropertyOptional({ default: false })
  @IsBoolean()
  @IsOptional()
  isHidden?: boolean;

  @ApiPropertyOptional({ default: false })
  @IsBoolean()
  @IsOptional()
  isEncrypted?: boolean;

  @ApiPropertyOptional({ default: 0 })
  @IsInt()
  @Min(0)
  @IsOptional()
  sortOrder?: number;

  @ApiPropertyOptional({ description: 'Wizard step identifier for multi-step forms' })
  @IsString()
  @IsOptional()
  stepId?: string;

  @ApiPropertyOptional({ description: 'Display label for the wizard step' })
  @IsString()
  @IsOptional()
  stepName?: string;

  @ApiPropertyOptional({ type: FieldConditionInputDto })
  @ValidateNested()
  @Type(() => FieldConditionInputDto)
  @IsOptional()
  condition?: FieldConditionInputDto;

  @ApiPropertyOptional({ type: DependentOptionsInputDto })
  @ValidateNested()
  @Type(() => DependentOptionsInputDto)
  @IsOptional()
  dependentOptions?: DependentOptionsInputDto;

  @ApiPropertyOptional({ type: FieldValidationRulesInputDto })
  @ValidateNested()
  @Type(() => FieldValidationRulesInputDto)
  @IsOptional()
  validationRules?: FieldValidationRulesInputDto;

  @ApiPropertyOptional({ type: [String] })
  @IsArray()
  @IsString({ each: true })
  @ArrayUnique()
  @IsOptional()
  viewPermissions?: string[];
}

export class UpdateFormFieldDto {
  @ApiPropertyOptional()
  @IsString()
  @IsOptional()
  label?: string;

  @ApiPropertyOptional({ enum: FIELD_TYPES })
  @IsIn(FIELD_TYPES)
  @IsOptional()
  fieldType?: CustomFieldType;

  @ApiPropertyOptional()
  @IsBoolean()
  @IsOptional()
  mandatory?: boolean;

  @ApiPropertyOptional({ type: [FieldOptionInputDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => FieldOptionInputDto)
  @IsOptional()
  fieldOptions?: FieldOptionInputDto[];

  @ApiPropertyOptional()
  @IsBoolean()
  @IsOptional()
  isHidden?: boolean;

  @ApiPropertyOptional()
  @IsBoolean()
  @IsOptional()
  isEncrypted?: boolean;

  @ApiPropertyOptional()
  @IsInt()
  @Min(0)
  @IsOptional()
  sortOrder?: number;

  @ApiPropertyOptional({ nullable: true, description: 'Wizard step identifier for multi-step forms' })
  @IsString()
  @IsOptional()
  stepId?: string | null;

  @ApiPropertyOptional({ nullable: true, description: 'Display label for the wizard step' })
  @IsString()
  @IsOptional()
  stepName?: string | null;

  @ApiPropertyOptional({ type: FieldConditionInputDto, nullable: true })
  @ValidateNested()
  @Type(() => FieldConditionInputDto)
  @IsOptional()
  condition?: FieldConditionInputDto | null;

  @ApiPropertyOptional({ type: DependentOptionsInputDto, nullable: true })
  @ValidateNested()
  @Type(() => DependentOptionsInputDto)
  @IsOptional()
  dependentOptions?: DependentOptionsInputDto | null;

  @ApiPropertyOptional({ type: FieldValidationRulesInputDto, nullable: true })
  @ValidateNested()
  @Type(() => FieldValidationRulesInputDto)
  @IsOptional()
  validationRules?: FieldValidationRulesInputDto | null;

  @ApiPropertyOptional({ type: [String] })
  @IsArray()
  @IsString({ each: true })
  @ArrayUnique()
  @IsOptional()
  viewPermissions?: string[];
}

export class FieldSortOrderItemDto {
  @ApiProperty()
  @IsString()
  @IsNotEmpty()
  id: string;

  @ApiProperty()
  @IsInt()
  @Min(0)
  sortOrder: number;
}

export class BulkUpdateFieldSortOrderDto {
  @ApiProperty({ type: [FieldSortOrderItemDto] })
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => FieldSortOrderItemDto)
  items: FieldSortOrderItemDto[];
}
