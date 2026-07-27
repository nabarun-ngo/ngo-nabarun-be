import { Inject } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import {
  IPublicSiteStaticContentPort,
  PUBLIC_SITE_STATIC_CONTENT_PORT,
} from '../../../domain/ports/public-site-static-content.port';
import { GetStaticContentQuery } from './get-static-content.query';

@QueryHandler(GetStaticContentQuery)
export class GetStaticContentHandler
  implements IQueryHandler<GetStaticContentQuery, Record<string, unknown>>
{
  constructor(
    @Inject(PUBLIC_SITE_STATIC_CONTENT_PORT)
    private readonly content: IPublicSiteStaticContentPort,
  ) {}

  execute(): Promise<Record<string, unknown>> {
    return this.content.getStaticContent();
  }
}
