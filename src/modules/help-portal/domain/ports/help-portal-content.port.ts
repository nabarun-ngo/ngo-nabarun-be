import { HelpArticleBlock } from '../../help-portal.schema';

export interface HelpPortalCategory {
  key: string;
  title: string;
  order?: number;
}

export interface HelpPortalArticleSummary {
  slug: string;
  title: string;
  categoryKey: string;
  summary?: string;
  order?: number;
  active: boolean;
  estimatedMinutes?: number;
}

export interface HelpPortalCatalog {
  categories: HelpPortalCategory[];
  featuredSlugs: string[];
  articles: HelpPortalArticleSummary[];
}

export interface HelpPortalArticle {
  slug: string;
  title: string;
  categoryKey: string;
  summary?: string;
  updatedAt?: string;
  relatedSlugs: string[];
  blocks: HelpArticleBlock[];
}

export const IHelpPortalContentPort = Symbol('IHelpPortalContentPort');

export interface IHelpPortalContentPort {
  getCatalog(): Promise<HelpPortalCatalog>;
  getArticle(slug: string): Promise<HelpPortalArticle | null>;
}
