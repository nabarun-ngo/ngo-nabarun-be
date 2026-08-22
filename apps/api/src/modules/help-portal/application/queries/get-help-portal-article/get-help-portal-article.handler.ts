import { Inject, Injectable, NotFoundException } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IHelpPortalContentPort } from '../../../domain/ports/help-portal-content.port';
import { HelpPortalArticleDto } from '../../dtos/help-portal.dto';
import { GetHelpPortalArticleQuery } from './get-help-portal-article.query';

@QueryHandler(GetHelpPortalArticleQuery)
@Injectable()
export class GetHelpPortalArticleHandler
  implements IQueryHandler<GetHelpPortalArticleQuery, HelpPortalArticleDto>
{
  constructor(@Inject(IHelpPortalContentPort) private readonly port: IHelpPortalContentPort) {}

  async execute(query: GetHelpPortalArticleQuery): Promise<HelpPortalArticleDto> {
    const article = await this.port.getArticle(query.slug);
    if (!article) {
      throw new NotFoundException(`Help article '${query.slug}' not found`);
    }
    return article;
  }
}
