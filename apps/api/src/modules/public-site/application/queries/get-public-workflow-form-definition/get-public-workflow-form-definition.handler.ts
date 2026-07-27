import { Inject } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { CustomFormsFacade } from '@nabarun-ngo/nestjs-shared-custom-forms';
import { EntityType } from '../../../../../shared/entity-type.enum';
import {
  resolveStartEventFormKey,
  WorkflowFacade,
} from '@nabarun-ngo/nestjs-shared-workflow';
import { resolvePublicWorkflowDefinitionId } from '../../helpers/resolve-public-workflow-definition-id';
import {
  mapPublishedFormToPublicDto,
  PublicFormDefinitionDto,
} from '../../mappers/public-form-definition.mapper';
import { PUBLIC_SITE_OPTIONS, PublicSiteOptions } from '../../../public-site.options';
import { GetPublicWorkflowFormDefinitionQuery } from './get-public-workflow-form-definition.query';

@QueryHandler(GetPublicWorkflowFormDefinitionQuery)
export class GetPublicWorkflowFormDefinitionHandler
  implements IQueryHandler<GetPublicWorkflowFormDefinitionQuery, PublicFormDefinitionDto>
{
  constructor(
    private readonly workflowFacade: WorkflowFacade,
    private readonly customFormsFacade: CustomFormsFacade,
    @Inject(PUBLIC_SITE_OPTIONS)
    private readonly options: PublicSiteOptions,
  ) {}

  async execute(
    query: GetPublicWorkflowFormDefinitionQuery,
  ): Promise<PublicFormDefinitionDto> {
    const definitionId = resolvePublicWorkflowDefinitionId(
      this.options,
      query.workflowName,
    );
    const definition = await this.workflowFacade.getDefinition(definitionId);
    const formKey = resolveStartEventFormKey(definition);
    const form = await this.customFormsFacade.getPublishedFormByKey(
      EntityType.Workflow,
      formKey,
    );

    return mapPublishedFormToPublicDto(query.workflowName, form);
  }
}
