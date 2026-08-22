import { ApiProperty, OmitType, PickType } from '@nestjs/swagger';
import {
  FormFieldDefinitionResponseDto,
  FormResponseDto,
} from '@nabarun-ngo/nestjs-shared-custom-forms';

/**
 * Swagger models for `PublicFormDefinitionDto` — the published custom-forms
 * definition with admin / audit properties stripped by
 * `mapPublishedFormToPublicDto`.
 */
export class PublicFormFieldDefinitionResponseDto extends OmitType(
  FormFieldDefinitionResponseDto,
  ['formId', 'viewPermissions', 'createdAt', 'updatedAt'] as const,
) {}

export class PublicFormDefinitionResponseDto extends PickType(FormResponseDto, [
  'label',
  'description',
] as const) {
  @ApiProperty({
    description: 'Route key the definition was requested with',
    example: 'volunteer-intake',
  })
  id!: string;

  @ApiProperty({
    description: 'Route key the definition was requested with',
    example: 'volunteer-intake',
  })
  key!: string;

  @ApiProperty({
    type: [PublicFormFieldDefinitionResponseDto],
    description: 'Enabled fields only, ordered by sortOrder',
  })
  fields!: PublicFormFieldDefinitionResponseDto[];
}
