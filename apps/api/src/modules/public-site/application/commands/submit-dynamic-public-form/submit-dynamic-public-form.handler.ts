import { Inject } from '@nestjs/common';
import { CommandHandler, ICommandHandler } from '@nestjs/cqrs';
import { CustomFormsFacade } from '@nabarun-ngo/nestjs-shared-custom-forms';
import { EntityType } from '../../../../../shared/enums/entity-type.enum';
import { PublicFormSubmitResponseDto } from '../../dtos/public-form-submit-response.dto';
import {
  PUBLIC_SITE_DEFAULT_SUBMITTED_BY_ID,
  PUBLIC_SITE_OPTIONS,
  PublicSiteOptions,
} from '../../../public-site.options';
import { SubmitDynamicPublicFormCommand } from './submit-dynamic-public-form.command';

@CommandHandler(SubmitDynamicPublicFormCommand)
export class SubmitDynamicPublicFormHandler
  implements ICommandHandler<SubmitDynamicPublicFormCommand, PublicFormSubmitResponseDto> {
  constructor(
    private readonly customFormsFacade: CustomFormsFacade,
    @Inject(PUBLIC_SITE_OPTIONS)
    private readonly options: PublicSiteOptions,
  ) { }

  async execute(command: SubmitDynamicPublicFormCommand): Promise<PublicFormSubmitResponseDto> {
    const form = await this.customFormsFacade.getPublishedFormByKey(
      EntityType.PublicSite,
      command.publicFormKey,
    );
    const submittedById =
      this.options.submittedById ?? PUBLIC_SITE_DEFAULT_SUBMITTED_BY_ID;

    const referenceId = await this.customFormsFacade.validateAndSubmit({
      formId: form.id,
      entityType: EntityType.PublicSite,
      entityId: `public:${command.publicFormKey}:${Date.now()}`,
      values: command.values,
      submittedById,
    });

    return {
      message: 'Form submitted successfully',
      referenceId,
    };
  }
}
