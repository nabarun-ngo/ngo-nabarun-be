import { Inject } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { randomUUID } from 'crypto';
import {
  WorkflowFacade,
  WorkflowRequesterType,
} from '@nabarun-ngo/nestjs-shared-workflow';
import { resolvePublicWorkflowDefinitionId } from '../../helpers/resolve-public-workflow-definition-id';
import { PublicFormSubmitResponseDto } from '../../dtos/public-form-submit-response.dto';
import {
  PUBLIC_SITE_DEFAULT_SUBMITTED_BY_ID,
  PUBLIC_SITE_OPTIONS,
  PublicSiteOptions,
} from '../../../public-site.options';
import { SubmitPublicWorkflowFormCommand } from './submit-public-workflow-form.command';

@CommandHandler(SubmitPublicWorkflowFormCommand)
export class SubmitPublicWorkflowFormHandler
  implements ICommandHandler<SubmitPublicWorkflowFormCommand, PublicFormSubmitResponseDto>
{
  constructor(
    private readonly workflowFacade: WorkflowFacade,
    @Inject(PUBLIC_SITE_OPTIONS)
    private readonly options: PublicSiteOptions,
  ) {}

  async execute(command: SubmitPublicWorkflowFormCommand): Promise<PublicFormSubmitResponseDto> {
    const definitionId = resolvePublicWorkflowDefinitionId(
      this.options,
      command.workflowName,
    );

    const submittedById =
      this.options.submittedById ?? PUBLIC_SITE_DEFAULT_SUBMITTED_BY_ID;

    const instance = await this.workflowFacade.startWorkflow({
      definitionId,
      requester: { type: WorkflowRequesterType.External, id: null },
      formValues: command.values,
      formSubmittedById: submittedById,
      idempotencyKey: `public:${definitionId}:${randomUUID()}`,

    });

    return {
      message: 'Request submitted successfully',
      referenceId: instance.id,
    };
  }
}
