import { PrismaClient } from '../../persistence/prisma/client';
import { JsonStoreSeedData } from './json-store-seed.types';

/** Ensures `Record<string, unknown>` satisfies Prisma's `InputJsonValue` constraint. */
function toInputJson(payload: Record<string, unknown>): any {
  return JSON.parse(JSON.stringify(payload));
}

export async function seedJsonStoreData(
  prisma: Pick<PrismaClient, 'jsonStoreDocument'>,
  data: JsonStoreSeedData,
): Promise<void> {
  for (const doc of data.documents) {
    const strategy = doc.onConflict ?? 'upsert';

    await prisma.jsonStoreDocument.upsert({
      where: {
        json_store_key_namespace_unique: { key: doc.key, namespace: doc.namespace },
      },
      // 'skip-if-exists': empty update object — record is found, nothing is changed.
      // 'upsert': payload is always replaced with the seed value.
      update: strategy === 'upsert' ? { payload: toInputJson(doc.payload) } : {},
      create: { key: doc.key, namespace: doc.namespace, payload: toInputJson(doc.payload) },
    });
  }
}
