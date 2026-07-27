#!/usr/bin/env node
/**
 * Build and publish a single workspace package to the configured npm registry.
 *
 * Usage:
 *   npm run publish:package -- @nabarun-ngo/nestjs-shared-core
 *   npm run publish:package -- @nabarun-ngo/nestjs-shared-core --dry-run
 */
import { execSync } from 'node:child_process';
import { readFileSync, existsSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const rootDir = resolve(__dirname, '..');

const args = process.argv.slice(2).filter((arg) => arg !== '--');
const dryRun = args.includes('--dry-run');
const packageName = args.find((arg) => !arg.startsWith('--'));

if (!packageName) {
  console.error('Usage: npm run publish:package -- <package-name> [--dry-run]');
  console.error('Example: npm run publish:package -- @nabarun-ngo/nestjs-shared-core');
  process.exit(1);
}

if (!packageName.startsWith('@nabarun-ngo/nestjs-shared-')) {
  console.error(`Refusing to publish non-library package: ${packageName}`);
  console.error('Only @nabarun-ngo/nestjs-shared-* packages may be published.');
  process.exit(1);
}

const workspaceDir = resolve(rootDir, 'packages', packageName.replace('@nabarun-ngo/', ''));
const packageJsonPath = resolve(workspaceDir, 'package.json');

if (!existsSync(packageJsonPath)) {
  console.error(`Package not found: ${packageName}`);
  console.error(`Expected: ${packageJsonPath}`);
  process.exit(1);
}

const pkg = JSON.parse(readFileSync(packageJsonPath, 'utf8'));

if (pkg.private) {
  console.error(`Package ${packageName} is marked private and cannot be published.`);
  process.exit(1);
}

function run(command) {
  console.log(`\n> ${command}`);
  execSync(command, { cwd: rootDir, stdio: 'inherit' });
}

console.log(`Publishing ${packageName}@${pkg.version}${dryRun ? ' (dry run)' : ''}`);

run(`npm run build --workspace=${packageName}`);

const publishCmd = dryRun
  ? `npm publish --workspace=${packageName} --dry-run`
  : `npm publish --workspace=${packageName}`;

run(publishCmd);

console.log(`\nDone.${dryRun ? ' No packages were published (dry run).' : ''}`);
