export type JsonStoreSchemaGroup = 'reference' | 'content' | 'managed';

export interface JsonStoreSchemaCatalogEntry {
  /** Registry key: `namespace` or `namespace:key`. */
  registryKey: string;
  namespace: string;
  /** Present when the registry entry is a per-key override. */
  key?: string;
  group: JsonStoreSchemaGroup;
  label: string;
  /** Relative admin URL when another console owns this namespace. */
  managedLink?: string;
  consumerHint?: string;
}

/**
 * UI/catalog metadata for registered JSON-store schemas.
 * Keep in sync with JSON_STORE_SCHEMA_REGISTRY keys.
 */
export const JSON_STORE_SCHEMA_CATALOG: JsonStoreSchemaCatalogEntry[] = [
  {
    registryKey: 'user-reference-data',
    namespace: 'user-reference-data',
    group: 'reference',
    label: 'Member lookups',
    consumerHint: 'Member titles, genders, geographies, document types',
  },
  {
    registryKey: 'finance-reference-data',
    namespace: 'finance-reference-data',
    group: 'reference',
    label: 'Finance lookups',
    consumerHint: 'Donation, expense, account, and payment catalogs',
  },
  {
    registryKey: 'project-reference-data',
    namespace: 'project-reference-data',
    group: 'reference',
    label: 'Project lookups',
    consumerHint: 'Statuses, types, priorities, and risk catalogs',
  },
  {
    registryKey: 'report-definitions',
    namespace: 'report-definitions',
    group: 'reference',
    label: 'Report definitions',
    consumerHint: 'Registered report catalog',
  },
  {
    registryKey: 'custom-forms.field-options',
    namespace: 'custom-forms.field-options',
    group: 'reference',
    label: 'Form field options',
    consumerHint: 'Dropdown option sets for custom forms',
  },
  {
    registryKey: 'links',
    namespace: 'links',
    group: 'content',
    label: 'Links',
    consumerHint: 'App links, guides, and policy collections',
  },
  {
    registryKey: 'links:user-guides',
    namespace: 'links',
    key: 'user-guides',
    group: 'content',
    label: 'User guides',
  },
  {
    registryKey: 'links:policies',
    namespace: 'links',
    key: 'policies',
    group: 'content',
    label: 'Policies',
  },
  {
    registryKey: 'links:app-links',
    namespace: 'links',
    key: 'app-links',
    group: 'content',
    label: 'App links',
  },
  {
    registryKey: 'links:link-open-types',
    namespace: 'links',
    key: 'link-open-types',
    group: 'content',
    label: 'Link open types',
  },
  {
    registryKey: 'links:app-link-types',
    namespace: 'links',
    key: 'app-link-types',
    group: 'content',
    label: 'App link types',
  },
  {
    registryKey: 'links:link-categories',
    namespace: 'links',
    key: 'link-categories',
    group: 'content',
    label: 'Link categories',
  },
  {
    registryKey: 'help-portal',
    namespace: 'help-portal',
    group: 'content',
    label: 'Help portal',
    consumerHint: 'Help catalog and articles',
  },
  {
    registryKey: 'help-portal:catalog',
    namespace: 'help-portal',
    key: 'catalog',
    group: 'content',
    label: 'Help catalog',
  },
  {
    registryKey: 'correspondence',
    namespace: 'correspondence',
    group: 'content',
    label: 'Email templates',
    consumerHint: 'Correspondence notification templates',
  },
  {
    registryKey: 'public-site',
    namespace: 'public-site',
    group: 'content',
    label: 'Public site',
    consumerHint: 'Public website static content',
  },
  {
    registryKey: 'cron',
    namespace: 'cron',
    group: 'managed',
    label: 'Cron jobs',
    managedLink: '/secured/admin/cron-jobs',
    consumerHint: 'Schedule definitions — prefer the Cron Jobs console',
  },
  {
    registryKey: 'request-definitions',
    namespace: 'request-definitions',
    group: 'content',
    label: 'Request definitions',
    consumerHint: 'Request types: fulfillment roles, approval gates, form keys',
  },
];
