import { z } from 'zod';

export const HELP_ARTICLE_BLOCK_TYPES = [
  'heading',
  'paragraph',
  'callout',
  'steps',
  'bullets',
  'video',
  'link',
] as const;

export const HELP_CALLOUT_TONES = ['tip', 'warning', 'info'] as const;

const HeadingBlockSchema = z.object({
  type: z.literal('heading'),
  level: z.union([z.literal(2), z.literal(3)]).optional(),
  text: z.string().min(1),
});

const ParagraphBlockSchema = z.object({
  type: z.literal('paragraph'),
  text: z.string().min(1),
});

const CalloutBlockSchema = z.object({
  type: z.literal('callout'),
  tone: z.enum(HELP_CALLOUT_TONES).optional(),
  text: z.string().min(1),
});

const StepsBlockSchema = z.object({
  type: z.literal('steps'),
  items: z.array(z.string().min(1)).min(1),
});

const BulletsBlockSchema = z.object({
  type: z.literal('bullets'),
  items: z.array(z.string().min(1)).min(1),
});

const VideoBlockSchema = z.object({
  type: z.literal('video'),
  url: z.string().url(),
  title: z.string().min(1).optional(),
});

const LinkBlockSchema = z.object({
  type: z.literal('link'),
  label: z.string().min(1),
  url: z.string().url(),
  external: z.boolean().optional(),
});

export const HelpArticleBlockSchema = z.discriminatedUnion('type', [
  HeadingBlockSchema,
  ParagraphBlockSchema,
  CalloutBlockSchema,
  StepsBlockSchema,
  BulletsBlockSchema,
  VideoBlockSchema,
  LinkBlockSchema,
]);

export const HelpPortalCategorySchema = z.object({
  key: z.string().min(1),
  title: z.string().min(1),
  order: z.number().int().optional(),
});

export const HelpPortalArticleSummarySchema = z.object({
  slug: z.string().min(1),
  title: z.string().min(1),
  categoryKey: z.string().min(1),
  summary: z.string().optional(),
  order: z.number().int().optional(),
  active: z.boolean().optional(),
  estimatedMinutes: z.number().positive().optional(),
});

export const HelpPortalCatalogPayloadSchema = z
  .object({
    categories: z.array(HelpPortalCategorySchema).min(1),
    featuredSlugs: z.array(z.string().min(1)).optional(),
    articles: z.array(HelpPortalArticleSummarySchema).min(1),
  })
  .passthrough();

export const HelpPortalArticlePayloadSchema = z
  .object({
    slug: z.string().min(1),
    title: z.string().min(1),
    categoryKey: z.string().min(1),
    summary: z.string().optional(),
    updatedAt: z.string().optional(),
    relatedSlugs: z.array(z.string().min(1)).optional(),
    blocks: z.array(HelpArticleBlockSchema).min(1),
  })
  .passthrough();

/** Namespace default: article payloads (per-slug keys). Catalog uses help-portal:catalog. */
export const HelpPortalPayloadSchema = z.union([
  HelpPortalCatalogPayloadSchema,
  HelpPortalArticlePayloadSchema,
]);

export type HelpPortalCatalogPayload = z.infer<typeof HelpPortalCatalogPayloadSchema>;
export type HelpPortalArticlePayload = z.infer<typeof HelpPortalArticlePayloadSchema>;
export type HelpArticleBlock = z.infer<typeof HelpArticleBlockSchema>;
