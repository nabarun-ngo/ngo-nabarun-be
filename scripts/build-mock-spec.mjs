#!/usr/bin/env node
/**
 * Derives `apps/api/swagger.mock.json` from the committed `apps/api/swagger.json`.
 *
 * The generated spec already carries an example on every request body, response
 * payload, and parameter (enforced by check-swagger-examples.mjs), so it needs
 * only two adjustments before Prism can serve it as a frontend mock:
 *
 *   1. A `servers` entry — the generated spec ships an empty array, which leaves
 *      `__server` validation and client codegen without a base URL.
 *   2. Removal of `security` — every operation requires a JWT or API key, and
 *      Prism answers 401 when those are absent. It has no flag to skip that
 *      check, so the requirement has to come out of the document itself.
 *
 * Usage (from apps/api, or via npm run mock:*):
 *   node scripts/build-mock-spec.mjs              # no auth required by the mock
 *   node scripts/build-mock-spec.mjs --keep-auth  # mock still demands a token
 *   node scripts/build-mock-spec.mjs --port 4020  # match a non-default Prism port
 */
import { readFileSync, writeFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, resolve } from 'node:path';

const HTTP_METHODS = ['get', 'post', 'put', 'delete', 'patch', 'options', 'head', 'trace'];

const here = dirname(fileURLToPath(import.meta.url));
const sourcePath = resolve(here, '..', 'swagger.json');
const targetPath = resolve(here, '..', 'swagger.mock.json');

const keepAuth = process.argv.includes('--keep-auth');
const portIndex = process.argv.indexOf('--port');
const port = portIndex === -1 ? '8080' : process.argv[portIndex + 1];

if (!/^\d+$/.test(port)) {
  process.stderr.write(`--port expects a number, received "${port}"\n`);
  process.exit(1);
}

let spec;
try {
  spec = JSON.parse(readFileSync(sourcePath, 'utf8'));
} catch (error) {
  process.stderr.write(
    `Could not read ${sourcePath}: ${String(error)}\n` +
      'Generate it first: npm run build --workspace=apps/api && npm run generate:swagger --workspace=apps/api\n',
  );
  process.exit(1);
}

spec.servers = [{ url: `http://localhost:${port}`, description: 'Prism mock server' }];

let strippedOperations = 0;

if (!keepAuth) {
  delete spec.security;

  for (const pathItem of Object.values(spec.paths ?? {})) {
    for (const method of HTTP_METHODS) {
      const operation = pathItem[method];
      if (operation?.security) {
        delete operation.security;
        strippedOperations += 1;
      }
    }
  }
}

writeFileSync(targetPath, JSON.stringify(spec, null, 2), 'utf8');

const pathCount = Object.keys(spec.paths ?? {}).length;
const operationCount = Object.values(spec.paths ?? {}).reduce(
  (total, pathItem) => total + HTTP_METHODS.filter((method) => pathItem[method]).length,
  0,
);

process.stdout.write(
  `swagger.mock.json written: ${pathCount} paths, ${operationCount} operations, ` +
    `base http://localhost:${port} — ` +
    (keepAuth
      ? 'auth kept (send Authorization: Bearer <anything> or X-Api-Key: <anything>)\n'
      : `auth stripped from ${strippedOperations} operations\n`),
);
