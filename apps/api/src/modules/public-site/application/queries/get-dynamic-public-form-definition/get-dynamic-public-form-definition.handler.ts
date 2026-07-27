import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { CustomFormsFacade } from '@nabarun-ngo/nestjs-shared-custom-forms';
import { EntityType } from '../../../../../shared/enums/entity-type.enum';
import {
  mapPublishedFormToPublicDto,
  PublicFormDefinitionDto,
} from '../../mappers/public-form-definition.mapper';
import { GetDynamicPublicFormDefinitionQuery } from './get-dynamic-public-form-definition.query';

@QueryHandler(GetDynamicPublicFormDefinitionQuery)
export class GetDynamicPublicFormDefinitionHandler
  implements IQueryHandler<GetDynamicPublicFormDefinitionQuery, PublicFormDefinitionDto> {
  constructor(private readonly customFormsFacade: CustomFormsFacade) { }

  async execute(
    query: GetDynamicPublicFormDefinitionQuery,
  ): Promise<PublicFormDefinitionDto> {
    const form = await this.customFormsFacade.getPublishedFormByKey(
      EntityType.PublicSite,
      query.publicFormKey,
    );
    return mapPublishedFormToPublicDto(query.publicFormKey, form);
  }
}
