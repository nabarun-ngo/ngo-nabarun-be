import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BusinessError } from '@nabarun-ngo/nestjs-shared-core';
import { CustomFormsFacade, FormNotFoundError } from '@nabarun-ngo/nestjs-shared-custom-forms';
import { EntityType } from '../../../../../shared/enums/entity-type.enum';
import { RequestDefinitionNotFoundError } from '../../../domain/errors/request.errors';
import { IRequestDefinitionPort } from '../../../domain/ports/request-definition.port';
import { RequestStartFormDto } from '../../dtos/request.dto';
import { GetRequestStartFormQuery } from './get-request-start-form.query';

const FORM_SERVICE_PERMISSIONS = [
  'admin:workflows',
  'read:custom_forms',
  'read:form_submissions',
  'write:form_submissions',
  'submit:form_submissions',
];

@QueryHandler(GetRequestStartFormQuery)
@Injectable()
export class GetRequestStartFormHandler
  implements IQueryHandler<GetRequestStartFormQuery, RequestStartFormDto>
{
  constructor(
    @Inject(IRequestDefinitionPort)
    private readonly definitions: IRequestDefinitionPort,
    private readonly customForms: CustomFormsFacade,
  ) {}

  async execute(query: GetRequestStartFormQuery): Promise<RequestStartFormDto> {
    const definition = await this.definitions.getDefinition(query.type);
    if (!definition) {
      throw new RequestDefinitionNotFoundError(query.type);
    }

    try {
      const form = await this.customForms.getPublishedFormByKey(
        EntityType.Workflow,
        definition.formKey,
        FORM_SERVICE_PERMISSIONS,
      );
      return {
        type: definition.id,
        formKey: definition.formKey,
        formId: form.id,
        label: form.label,
        description: form.description ?? null,
        fields: (form.fields ?? [])
          .filter((field) => field.enabled)
          .sort((a, b) => a.sortOrder - b.sortOrder)
          .map((field) => ({
            id: field.id,
            key: field.key,
            label: field.label,
            fieldType: field.fieldType,
            mandatory: field.mandatory,
            fieldOptions: (field.fieldOptions ?? []).map((o) => ({
              key: o.key,
              label: o.label,
            })),
            isHidden: field.isHidden,
            isEncrypted: field.isEncrypted,
            enabled: field.enabled,
            sortOrder: field.sortOrder,
            stepId: field.stepId ?? null,
            stepName: field.stepName ?? null,
            condition: field.condition ?? null,
            dependentOptions: field.dependentOptions ?? null,
            validationRules: field.validationRules ?? null,
          })),
      };
    } catch (error) {
      if (
        error instanceof FormNotFoundError ||
        (error instanceof BusinessError && error.errorCode === 'CUSTOM_FORM_NOT_FOUND')
      ) {
        return {
          type: definition.id,
          formKey: definition.formKey,
          formId: null,
          label: null,
          description: null,
          fields: [],
        };
      }
      throw error;
    }
  }
}
