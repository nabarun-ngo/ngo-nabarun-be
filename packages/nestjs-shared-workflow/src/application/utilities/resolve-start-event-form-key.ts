import type { WorkflowDefinition } from '../../dsl/workflow-definition.schema';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';

export function resolveStartEventFormKey(definition: WorkflowDefinition): string {
  const start = definition.elements.find((el) => el.type === 'startEvent');
  if (!start || start.type !== 'startEvent') {
    throw new BusinessException(
      `Workflow "${definition.id}" has no startEvent element`,
    );
  }
  if (!start.formKey?.trim()) {
    throw new BusinessException(
      `Workflow "${definition.id}" startEvent is missing formKey`,
    );
  }
  return start.formKey;
}
