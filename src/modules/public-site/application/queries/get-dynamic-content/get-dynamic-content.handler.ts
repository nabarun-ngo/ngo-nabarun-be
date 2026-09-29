import { Inject } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import {
  IPublicSiteDynamicContentPort,
} from '../../../domain/ports/public-site-dynamic-content.port';
import { PublicSiteDynamicContent } from '../../../public-site.schema';
import { GetDynamicContentQuery } from './get-dynamic-content.query';

@QueryHandler(GetDynamicContentQuery)
export class GetDynamicContentHandler
  implements IQueryHandler<GetDynamicContentQuery, PublicSiteDynamicContent>
{
  constructor(
    @Inject(IPublicSiteDynamicContentPort)
    private readonly content: IPublicSiteDynamicContentPort,
  ) {}

  execute(): Promise<PublicSiteDynamicContent> {
    return this.content.getDynamicContent();
  }
}
