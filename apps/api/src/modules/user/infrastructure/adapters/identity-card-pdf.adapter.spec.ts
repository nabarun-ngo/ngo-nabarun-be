import { existsSync, mkdirSync, readFileSync, writeFileSync } from 'fs';
import { join } from 'path';
import type { IdentityCardPdfInput } from '../../domain/ports/identity-card-pdf.port';
import { IdentityCardPdfAdapter, printIdentityCard } from './identity-card-pdf.adapter';

const PNG_MAGIC = Buffer.from([0x89, 0x50, 0x4e, 0x47]);

describe('IdentityCardPdfAdapter (no HTTP server)', () => {
  // Each case starts a real Chromium, which is far slower when the full suite saturates the workers.
  jest.setTimeout(180_000);

  const sample: IdentityCardPdfInput = {
    organisationName: 'Ichapur Nabarun Social Welfare Society',
    organisationRegistrationNumber: 'S0063530',
    organisationLogoDataUrl: loadLocalLogo(),
    displayName: 'Ananya Chatterjee',
    roleLabel: 'Member',
    initials: 'AC',
    uniqueMemberId: 'NM26030429',
    contactNumber: '+91 90000 00000',
    bloodGroup: 'B+',
    verifyUrl: 'https://api.example.test/public/identity-cards/NM26030429',
  };

  it('renders a CR80 PDF through the same printer the API uses', async () => {
    const pdf = await new IdentityCardPdfAdapter().render(sample);
    expect(pdf.subarray(0, 4).toString()).toBe('%PDF');
    expect(pdf.length).toBeGreaterThan(1_000);
  });

  it('writes a sample PDF and PNG for visual review', async () => {
    const { pdf, png } = await printIdentityCard(sample);

    expect(pdf.subarray(0, 4).toString()).toBe('%PDF');
    expect(png.subarray(0, 4).equals(PNG_MAGIC)).toBe(true);

    const outDir = join(__dirname, '../../../../../test-output/identity-card');
    mkdirSync(outDir, { recursive: true });
    writeFileSync(join(outDir, 'identity-card.pdf'), pdf);
    writeFileSync(join(outDir, 'identity-card.png'), png);

    const previewDir = findWireframesDir();
    if (previewDir) {
      writeFileSync(join(previewDir, 'identity-card-print.pdf'), pdf);
      writeFileSync(join(previewDir, 'identity-card-print.png'), png);
    }
  });
});

function loadLocalLogo(): string | undefined {
  const candidates = [
    join(__dirname, '../../../../../../../../fe-monorepo/apps/internal-app/src/assets/logo.png'),
    join(process.cwd(), '../fe-monorepo/apps/internal-app/src/assets/logo.png'),
    join(process.cwd(), '../../fe-monorepo/apps/internal-app/src/assets/logo.png'),
  ];
  const path = candidates.find((candidate) => existsSync(candidate));
  if (!path) return undefined;
  return `data:image/png;base64,${readFileSync(path).toString('base64')}`;
}

function findWireframesDir(): string | undefined {
  const candidates = [
    join(__dirname, '../../../../../../../../.cursor/docs/wireframes'),
    join(process.cwd(), '../.cursor/docs/wireframes'),
    join(process.cwd(), '../../.cursor/docs/wireframes'),
  ];
  return candidates.find((candidate) => existsSync(candidate));
}
