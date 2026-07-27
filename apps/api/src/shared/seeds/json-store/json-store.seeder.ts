import { join } from 'path';
import { loadJsonStoreSeedFromDir } from './json-store-seed.loader';
import { seedJsonStoreData } from './json-store-seed.runner';
import { validateJsonStoreSeedData } from './validate-json-store-seed-data';
import { PrismaClient } from '../../persistence/prisma/client';
import { ZodJsonDocumentPayloadValidatorAdapter } from '../../integrations/json-store/json-document-payload-validator.adapter';

export { seedJsonStoreData } from './json-store-seed.runner';

export async function seedJsonStore(prisma: PrismaClient): Promise<void> {

  try {
    console.log("[json-store-seeder] Starting json-store seed...")
    const data = loadJsonStoreSeedFromDir(
      join(__dirname, 'data'),
    );
    validateJsonStoreSeedData(data, new ZodJsonDocumentPayloadValidatorAdapter());
    console.log(`[json-store-seeder] Seeding ${data.documents.length} document(s)...`);

    for (const doc of data.documents) {
      const strategy = doc.onConflict ?? 'upsert';
      console.log(`[json-store-seeder]   [${strategy}] ${doc.namespace}/${doc.key}`);
    }

    await seedJsonStoreData(prisma, data);

    console.log('[json-store-seeder] Done.');
  } catch (error) {
    console.log('[json-store-seeder] Failed.');

    console.error(error);
  }
}
