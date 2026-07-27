import {
  FormFieldDefinitionResponseDto,
  FormResponseDto,
} from '@nabarun-ngo/nestjs-shared-custom-forms';

/** Public field shape: custom-forms definition minus admin / audit props. */
export type PublicFormFieldDefinitionDto = Omit<
  FormFieldDefinitionResponseDto,
  'formId' | 'viewPermissions' | 'createdAt' | 'updatedAt'
>;

/** Public GET form definition: route identity + published form metadata and fields. */
export type PublicFormDefinitionDto = Pick<
  FormResponseDto,
  'label' | 'description'
> & {
  id: string;
  key: string;
  fields: PublicFormFieldDefinitionDto[];
};

export function mapPublishedFormToPublicDto(
  routeKey: string,
  form: Pick<FormResponseDto, 'label' | 'description' | 'fields'>,
): PublicFormDefinitionDto {
  const fields = (form.fields ?? [])
    .filter((field) => field.enabled)
    .sort((a, b) => a.sortOrder - b.sortOrder)
    .map(stripFieldForPublic);

  return {
    id: routeKey,
    key: routeKey,
    label: form.label,
    description: form.description,
    fields,
  };
}

function stripFieldForPublic(
  field: FormFieldDefinitionResponseDto,
): PublicFormFieldDefinitionDto {
  const { formId: _formId, viewPermissions: _viewPermissions, createdAt: _createdAt, updatedAt: _updatedAt, ...publicField } =
    field;
  return publicField;
}
