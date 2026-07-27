import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { PublicSiteOptions } from '../../public-site.options';

export function resolvePublicWorkflowDefinitionId(
  options: PublicSiteOptions,
  workflowName: string,
): string {
  const binding = options.publicWorkflows[workflowName];
  if (!binding) {
    throw new BusinessException(
      `Public workflow "${workflowName}" is not configured`,
      'PUBLIC_WORKFLOW_NOT_CONFIGURED',
      404,
    );
  }
  return binding.definitionId;
}
