import { Injectable } from '@nestjs/common';
import { z } from 'zod';
import { JSON_STORE_SCHEMA_CATALOG } from './json-store-schema.catalog';
import {
  JsonStoreSchemaCatalogItemDto,
  JsonStoreSchemaResolveDto,
} from './json-store-schema.dtos';
import {
  JSON_STORE_SCHEMA_REGISTRY,
  resolveJsonStoreSchema,
} from './json-store-schema.registry';

@Injectable()
export class JsonStoreSchemaService {
  resolve(namespace: string, key?: string): JsonStoreSchemaResolveDto {
    const exactKey = key ? `${namespace}:${key}` : undefined;
    if (exactKey && JSON_STORE_SCHEMA_REGISTRY[exactKey]) {
      return {
        namespace,
        key,
        match: 'exact',
        jsonSchema: this.toJsonSchema(JSON_STORE_SCHEMA_REGISTRY[exactKey]),
      };
    }

    if (JSON_STORE_SCHEMA_REGISTRY[namespace]) {
      return {
        namespace,
        key,
        match: 'namespace',
        jsonSchema: this.toJsonSchema(JSON_STORE_SCHEMA_REGISTRY[namespace]),
      };
    }

    // Still try resolve helper for consistency when key is empty string etc.
    const resolved = key
      ? resolveJsonStoreSchema(JSON_STORE_SCHEMA_REGISTRY, namespace, key)
      : JSON_STORE_SCHEMA_REGISTRY[namespace];

    if (resolved) {
      const match =
        exactKey && JSON_STORE_SCHEMA_REGISTRY[exactKey] ? 'exact' : 'namespace';
      return { namespace, key, match, jsonSchema: this.toJsonSchema(resolved) };
    }

    return { namespace, key, match: 'none', jsonSchema: null };
  }

  listCatalog(): JsonStoreSchemaCatalogItemDto[] {
    const registryKeys = new Set(Object.keys(JSON_STORE_SCHEMA_REGISTRY));
    const fromCatalog = JSON_STORE_SCHEMA_CATALOG.map((entry) => ({
      registryKey: entry.registryKey,
      namespace: entry.namespace,
      key: entry.key,
      group: entry.group,
      label: entry.label,
      managedLink: entry.managedLink,
      consumerHint: entry.consumerHint,
      hasSchema: registryKeys.has(entry.registryKey),
    }));

    // Include any registry keys missing from the static catalog.
    const catalogKeys = new Set(fromCatalog.map((e) => e.registryKey));
    for (const registryKey of registryKeys) {
      if (catalogKeys.has(registryKey)) continue;
      const [namespace, key] = registryKey.includes(':')
        ? ([registryKey.slice(0, registryKey.indexOf(':')), registryKey.slice(registryKey.indexOf(':') + 1)] as const)
        : ([registryKey, undefined] as const);
      fromCatalog.push({
        registryKey,
        namespace,
        key,
        group: 'content',
        label: registryKey,
        managedLink: undefined,
        consumerHint: undefined,
        hasSchema: true,
      });
    }

    return fromCatalog;
  }

  private toJsonSchema(schema: z.ZodType): Record<string, unknown> {
    return z.toJSONSchema(schema, { unrepresentable: 'any' }) as Record<string, unknown>;
  }
}
