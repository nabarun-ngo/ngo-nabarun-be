import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IHelpPortalContentPort } from '../../../domain/ports/help-portal-content.port';
import { HelpPortalCatalogDto } from '../../dtos/help-portal.dto';
import { GetHelpPortalCatalogQuery } from './get-help-portal-catalog.query';

@QueryHandler(GetHelpPortalCatalogQuery)
@Injectable()
export class GetHelpPortalCatalogHandler
  implements IQueryHandler<GetHelpPortalCatalogQuery, HelpPortalCatalogDto>
{
  constructor(@Inject(IHelpPortalContentPort) private readonly port: IHelpPortalContentPort) {}

  async execute(): Promise<HelpPortalCatalogDto> {
    return this.port.getCatalog();
  }
}
