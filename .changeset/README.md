# Changesets

This monorepo uses [Changesets](https://github.com/changesets/changesets) for independent package versioning and publishing.

## Workflow

1. **Record a change** — after implementing a feature or fix:

   ```powershell
   npm run changeset
   ```

   Select the affected `@nabarun-ngo/nestjs-shared-*` packages and choose patch / minor / major.

2. **Apply version bumps** — when ready to release (usually on merge to `main`):

   ```powershell
   npm run version-packages
   ```

   This bumps `package.json` versions, updates internal dependency ranges, and generates changelogs.

3. **Publish all changed packages**:

   ```powershell
   npm run release
   ```

   Builds the monorepo, then publishes every package with a pending version bump.

4. **Publish a single package** (manual / hotfix):

   ```powershell
   npm run publish:package -- @nabarun-ngo/nestjs-shared-correspondence
   ```

   Builds that workspace and its dependencies, then runs `npm publish`.

## Registry

GitHub Packages is configured in the root [`.npmrc`](../.npmrc). Set `NPM_TOKEN` before publishing locally or in CI.

```powershell
$env:NPM_TOKEN = "<automation-token>"
npm whoami --registry=https://npm.pkg.github.com
```

For Azure Artifacts, uncomment the alternate registry block in `.npmrc`.

## Notes

- Internal package dependencies use `"*"` for npm workspace linking during development.
  `npm run version-packages` (Changesets) rewrites these to semver ranges (e.g. `^1.0.1`) in published manifests.
- `changeset status` requires a git repository with a `main` branch synced to remote.
- `apps/api` is excluded from versioning via the `ignore` list in `.changeset/config.json`.
