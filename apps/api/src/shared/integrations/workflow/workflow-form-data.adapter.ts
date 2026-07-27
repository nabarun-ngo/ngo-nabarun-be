import { Injectable } from '@nestjs/common';
import { BusinessError } from '@nabarun-ngo/nestjs-shared-core';
import {
  CustomFormsFacade,
  FormNotFoundError,
  ResolvedFormFieldValueResponseDto,
} from '@nabarun-ngo/nestjs-shared-custom-forms';
import {
  IWorkflowFormDataPort,
  WORKFLOW_FORM_DATA_PORT,
  WorkflowFormDataSnapshot,
} from '@nabarun-ngo/nestjs-shared-workflow';

import { EntityType } from '../../enums/entity-type.enum';

/** Permissions used for workflow-engine internal form delegation. */
const WORKFLOW_FORM_SERVICE_PERMISSIONS = [
  'admin:workflows',
  'read:form_submissions',
  'write:form_submissions',
  'submit:form_submissions',
];

@Injectable()
export class WorkflowFormDataAdapter implements IWorkflowFormDataPort {
  constructor(private readonly customFormsFacade: CustomFormsFacade) { }

  async getFormData(params: {
    instanceId: string;
    elementId: string;
    formKey: string;
    entityType?: string;
    entityId?: string;
  }): Promise<WorkflowFormDataSnapshot | null> {
    const scope = this.resolveScope(params);
    const formId = await this.customFormsFacade.resolveFormIdForKey(
      scope.entityType,
      params.formKey,
      WORKFLOW_FORM_SERVICE_PERMISSIONS,
    );

    const fields = await this.customFormsFacade.getFormSubmissionFields({
      formId,
      entityType: scope.entityType,
      entityId: scope.entityId,
      userId: `workflow:${params.instanceId}`,
      userPermissions: WORKFLOW_FORM_SERVICE_PERMISSIONS,
    });

    if (!fields.length) return null;

    return {
      formKey: params.formKey,
      entityType: scope.entityType,
      entityId: scope.entityId,
      values: this.toValuesMap(fields),
    };
  }

  async saveFormData(params: {
    instanceId: string;
    elementId: string;
    formKey: string;
    entityType?: string;
    entityId?: string;
    values: Record<string, unknown>;
    submittedById: string;
    submit: boolean;
  }): Promise<WorkflowFormDataSnapshot> {
    const scope = this.resolveScope(params);
    const formId = await this.customFormsFacade.resolveFormIdForKey(
      scope.entityType,
      params.formKey,
      WORKFLOW_FORM_SERVICE_PERMISSIONS,
    );
    const draftParams = {
      formId,
      entityType: scope.entityType,
      entityId: scope.entityId,
      values: params.values,
      userId: params.submittedById,
      submittedById: params.submittedById,
      userPermissions: WORKFLOW_FORM_SERVICE_PERMISSIONS,
    };

    if (params.submit) {
      await this.customFormsFacade.submitForm(draftParams);
    } else {
      await this.customFormsFacade.saveFormDraft(draftParams);
    }

    const snapshot = await this.getFormData(params);
    return {
      formKey: params.formKey,
      entityType: scope.entityType,
      entityId: scope.entityId,
      values: snapshot?.values ?? params.values,
      submittedAt: params.submit ? new Date() : undefined,
      submittedById: params.submittedById,
    };
  }

  async validateFormData(params: {
    formKey: string;
    entityType?: string;
    values: Record<string, unknown>;
  }): Promise<{ valid: boolean; errors?: Record<string, string[]> }> {
    const entityType = params.entityType ?? EntityType.Workflow;
    const formId = await this.customFormsFacade.resolveFormIdForKey(
      entityType,
      params.formKey,
      WORKFLOW_FORM_SERVICE_PERMISSIONS,
    );
    const draftEntityId = `__validate__:${formId}:${Date.now()}`;
    const entityParams = {
      formId,
      entityType,
      entityId: draftEntityId,
      userId: 'workflow:validator',
      userPermissions: WORKFLOW_FORM_SERVICE_PERMISSIONS,
    };

    await this.customFormsFacade.saveFormDraft({
      ...entityParams,
      values: params.values,
      submittedById: 'workflow:validator',
    });

    try {
      const result = await this.customFormsFacade.validateFormSubmission(entityParams);

      if (result.valid) {
        return { valid: true };
      }

      return {
        valid: false,
        errors: this.mapValidationErrors(result),
      };
    } catch (error) {
      if (error instanceof BusinessError && error.errorCode === 'CUSTOM_FORM_NOT_FOUND') {
        return { valid: false, errors: { _form: ['Form not found'] } };
      }
      if (error instanceof FormNotFoundError) {
        return { valid: false, errors: { _form: ['Form not found'] } };
      }
      throw error;
    } finally {
      await this.customFormsFacade.clearFormSubmission({
        ...entityParams,
        clearedById: 'workflow:validator',
      });
    }
  }

  private resolveScope(params: {
    instanceId: string;
    elementId: string;
    formKey: string;
    entityType?: string;
    entityId?: string;
  }): { entityType: string; entityId: string } {
    const entityType = params.entityType ?? EntityType.Workflow;
    const entityId =
      params.entityId ??
      (params.formKey.endsWith(':request')
        ? params.instanceId
        : `${params.instanceId}:${params.elementId}`);

    return { entityType, entityId };
  }

  private toValuesMap(
    fields: ResolvedFormFieldValueResponseDto[],
  ): Record<string, unknown> {
    const values: Record<string, unknown> = {};
    for (const field of fields) {
      if (field.value !== null && field.value !== undefined) {
        values[field.key] = field.value;
      }
    }
    return values;
  }

  private mapValidationErrors(result: {
    missingMandatory: string[];
    conditionViolations: string[];
    validationViolations: string[];
  }): Record<string, string[]> {
    const errors: Record<string, string[]> = {};

    for (const key of result.missingMandatory) {
      errors[key] = [...(errors[key] ?? []), 'This field is required'];
    }
    for (const key of result.conditionViolations) {
      errors[key] = [...(errors[key] ?? []), 'Condition not satisfied'];
    }
    for (const key of result.validationViolations) {
      errors[key] = [...(errors[key] ?? []), 'Validation failed'];
    }

    return errors;
  }
}

export const WORKFLOW_FORM_DATA_PROVIDER = {
  provide: WORKFLOW_FORM_DATA_PORT,
  useClass: WorkflowFormDataAdapter,
};
