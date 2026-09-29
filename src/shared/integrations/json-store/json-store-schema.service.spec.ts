import { JsonStoreSchemaService } from './json-store-schema.service';
import {
  formatZodValidationErrors,
  resolveJsonStoreSchema,
  JSON_STORE_SCHEMA_REGISTRY,
} from './json-store-schema.registry';
import { z } from 'zod';

describe('JsonStoreSchemaService', () => {
  const service = new JsonStoreSchemaService();

  it('resolves exact key override (links:policies)', () => {
    const result = service.resolve('links', 'policies');
    expect(result.match).toBe('exact');
    expect(result.jsonSchema).toBeTruthy();
    expect(result.namespace).toBe('links');
    expect(result.key).toBe('policies');
  });

  it('falls back to namespace schema when key has no override', () => {
    const result = service.resolve('correspondence', 'welcome-email');
    expect(result.match).toBe('namespace');
    expect(result.jsonSchema).toBeTruthy();
  });

  it('returns match none for unknown namespace', () => {
    const result = service.resolve('does-not-exist', 'anything');
    expect(result.match).toBe('none');
    expect(result.jsonSchema).toBeNull();
  });

  it('lists catalog entries with hasSchema true for registered keys', () => {
    const catalog = service.listCatalog();
    const finance = catalog.find((e) => e.registryKey === 'finance-reference-data');
    expect(finance).toBeDefined();
    expect(finance?.hasSchema).toBe(true);
    expect(finance?.group).toBe('reference');

    const cron = catalog.find((e) => e.registryKey === 'cron');
    expect(cron?.group).toBe('managed');
    expect(cron?.managedLink).toContain('cron-jobs');
  });
});

describe('formatZodValidationErrors (FE parse contract)', () => {
  it('emits path: message joined by "; "', () => {
    const schema = z.object({
      items: z.array(z.object({ code: z.string(), label: z.string().min(1) })),
    });
    const result = schema.safeParse({ items: [{ code: 12, label: '' }] });
    expect(result.success).toBe(false);
    if (result.success) return;

    const formatted = formatZodValidationErrors(result.error);
    // Contract used by the admin JSON Store editor to map server errors.
    expect(formatted).toMatch(/items\.0\.code:/);
    expect(formatted).toContain('; ');
    const parts = formatted.split('; ').map((p) => p.trim()).filter(Boolean);
    for (const part of parts) {
      expect(part).toMatch(/^.+: .+$/);
    }
  });
});

describe('resolveJsonStoreSchema', () => {
  it('prefers namespace:key over namespace', () => {
    const exact = resolveJsonStoreSchema(JSON_STORE_SCHEMA_REGISTRY, 'help-portal', 'catalog');
    const ns = resolveJsonStoreSchema(JSON_STORE_SCHEMA_REGISTRY, 'help-portal', 'other-article');
    expect(exact).toBe(JSON_STORE_SCHEMA_REGISTRY['help-portal:catalog']);
    expect(ns).toBe(JSON_STORE_SCHEMA_REGISTRY['help-portal']);
  });
});
