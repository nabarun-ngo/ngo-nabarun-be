import { Injectable, Logger } from '@nestjs/common';
import { JsonStoreFacade } from '@nabarun-ngo/nestjs-shared-json-store';
import { JsonStoreNameSpace } from '../../../../shared/enums/json-store-namespaces';
import {
  HelpPortalArticlePayloadSchema,
  HelpPortalCatalogPayloadSchema,
} from '../../help-portal.schema';
import {
  HelpPortalArticle,
  HelpPortalCatalog,
  IHelpPortalContentPort,
} from '../../domain/ports/help-portal-content.port';

@Injectable()
export class HelpPortalContentAdapter implements IHelpPortalContentPort {
  private static readonly NAMESPACE = JsonStoreNameSpace.HelpPortal;
  private readonly logger = new Logger(HelpPortalContentAdapter.name);

  constructor(private readonly jsonStore: JsonStoreFacade) {}

  async getCatalog(): Promise<HelpPortalCatalog> {
    const payload = await this.jsonStore.get('catalog', HelpPortalContentAdapter.NAMESPACE);
    if (!payload) {
      this.logger.warn('help-portal/catalog not found');
      return { categories: [], featuredSlugs: [], articles: [] };
    }
    const parsed = HelpPortalCatalogPayloadSchema.safeParse(payload);
    if (!parsed.success) {
      this.logger.warn(`Invalid help-portal/catalog payload: ${parsed.error.message}`);
      return { categories: [], featuredSlugs: [], articles: [] };
    }
    return {
      categories: [...parsed.data.categories].sort(
        (a, b) => (a.order ?? 0) - (b.order ?? 0),
      ),
      featuredSlugs: parsed.data.featuredSlugs ?? [],
      articles: parsed.data.articles
        .filter((a) => a.active !== false)
        .map((a) => ({
          slug: a.slug,
          title: a.title,
          categoryKey: a.categoryKey,
          summary: a.summary,
          order: a.order,
          active: a.active ?? true,
          estimatedMinutes: a.estimatedMinutes,
        }))
        .sort((a, b) => (a.order ?? 0) - (b.order ?? 0)),
    };
  }

  async getArticle(slug: string): Promise<HelpPortalArticle | null> {
    const payload = await this.jsonStore.get(slug, HelpPortalContentAdapter.NAMESPACE);
    if (!payload) return null;
    const parsed = HelpPortalArticlePayloadSchema.safeParse(payload);
    if (!parsed.success) {
      this.logger.warn(`Invalid help-portal/${slug} payload: ${parsed.error.message}`);
      return null;
    }
    return {
      slug: parsed.data.slug,
      title: parsed.data.title,
      categoryKey: parsed.data.categoryKey,
      summary: parsed.data.summary,
      updatedAt: parsed.data.updatedAt,
      relatedSlugs: parsed.data.relatedSlugs ?? [],
      blocks: parsed.data.blocks,
    };
  }
}
