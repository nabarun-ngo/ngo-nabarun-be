import 'reflect-metadata';
import { writeFileSync } from 'node:fs';
import { resolve } from 'node:path';
import { RequestMethod } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { buildSwaggerDocument } from '@nabarun-ngo/nestjs-shared-core';
import { AppModule } from './app.module';
import { prefixExclusions } from './main';

/**
 * Writes `apps/api/swagger.json` from the compiled application.
 *
 * Must run against `dist/` rather than through ts-node: the `@nestjs/swagger`
 * CLI plugin injects schema metadata at compile time, so a source-level run
 * would emit a spec missing every inferred type.
 *
 * Preview mode builds the DI graph without instantiating providers or firing
 * lifecycle hooks, so no database, Redis, or outbound credential is required.
 */
async function main() {
  const app = await NestFactory.create(AppModule, {
    preview: true,
    logger: false,
    abortOnError: false,
  });

  // Must mirror main.ts, otherwise every documented path loses its /api prefix.
  app.setGlobalPrefix('api', {
    exclude: prefixExclusions,
  });

  // Title and description are pinned rather than read from APP_NAME so the
  // committed spec does not churn with each developer's local .env.
  const document = buildSwaggerDocument(app, {
    title: 'NABARUN Mock API',
    description: 'NABARUN Mock application backend',
    version: '1.0',
  });

  const target = resolve(__dirname, '..', 'swagger.json');
  writeFileSync(target, JSON.stringify(document, null, 2), 'utf8');

  await app.close();
  process.stdout.write(
    `swagger.json written: ${Object.keys(document.paths).length} paths, ` +
    `${Object.keys(document.components?.schemas ?? {}).length} schemas\n`,
  );
}

main().catch((error: unknown) => {
  process.stderr.write(`Failed to generate swagger.json: ${String(error)}\n`);
  process.exit(1);
});
