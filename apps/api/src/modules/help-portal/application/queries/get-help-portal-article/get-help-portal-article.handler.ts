import { Inject, Injectable, Logger, NotFoundException } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IHelpPortalContentPort } from '../../../domain/ports/help-portal-content.port';
import { HelpPortalArticleDto } from '../../dtos/help-portal.dto';
import { GetHelpPortalArticleQuery } from './get-help-portal-article.query';

@QueryHandler(GetHelpPortalArticleQuery)
@Injectable()
export class GetHelpPortalArticleHandler
  implements IQueryHandler<GetHelpPortalArticleQuery, HelpPortalArticleDto>
{
  private readonly logger = new Logger(GetHelpPortalArticleHandler.name);

  constructor(@Inject(IHelpPortalContentPort) private readonly port: IHelpPortalContentPort) {}

  async execute(query: GetHelpPortalArticleQuery): Promise<HelpPortalArticleDto> {
    const article = await this.port.getArticle(query.slug);
    if (!article) {
      this.logger.warn(`Help article not found: ${query.slug}`);
      throw new NotFoundException('Help article not found');
    }
    return article;
  }
}
