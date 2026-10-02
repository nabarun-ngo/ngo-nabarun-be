import { Inject } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import {
  IPublicSiteStaticContentPort,
} from '../../../domain/ports/public-site-static-content.port';
import { GetStaticContentQuery } from './get-static-content.query';

@QueryHandler(GetStaticContentQuery)
export class GetStaticContentHandler
  implements IQueryHandler<GetStaticContentQuery, Record<string, unknown>>
{
  constructor(
    @Inject(IPublicSiteStaticContentPort)
    private readonly content: IPublicSiteStaticContentPort,
  ) {}

  execute(): Promise<Record<string, unknown>> {
    return this.content.getStaticContent();
  }
}
