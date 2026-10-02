import { ApiProperty } from '@nestjs/swagger';

/**
 * Static site copy is an operator-managed JSON document served verbatim from the
 * JSON store (`public-site` / `static-content`). The three top-level sections below
 * are the shape the site ships with; nested keys are free-form and evolve with the
 * front-end, so they are documented by example rather than by schema.
 *
 * `{{vars.*}}` / `{{metadata.*}}` placeholders inside string values are resolved
 * by the consuming site, not by the API.
 */
export class PublicSiteStaticContentResponseDto {
  @ApiProperty({
    type: Object,
    description: 'Shared values interpolated into the rest of the document',
    example: {
      orgName: 'Ichapur Nabarun Welfare Society',
      orgShortName: 'NABARUN',
      email: 'nabarunbangla18@gmail.com',
      siteUrl: 'https://nabarunngo.web.app',
    },
  })
  vars!: Record<string, unknown>;

  @ApiProperty({
    type: Object,
    description: 'SEO and organisation metadata, plus per-page title/description/sitemap entries',
    example: {
      site: { title: 'Nabarun NGO | Empowering Communities', brand: 'Nabarun', locale: 'en_IN' },
      organization: { logo: '{{vars.siteUrl}}{{vars.logoUrl}}', foundingDate: '{{vars.estd}}' },
      pages: { home: { pageName: 'Home', path: '/', sitemap: { priority: 1, changefreq: 'weekly' } } },
    },
  })
  metadata!: Record<string, unknown>;

  @ApiProperty({
    type: Object,
    description: 'Navigation, footer and per-page section copy for the public site',
    example: {
      common: {
        navbar: { brand: { name: '{{vars.orgName}}' }, moreLabel: 'More' },
        footer: { newsletter: { title: 'Stay Updated', button: 'Subscribe' } },
      },
      pages: { home: { heroStats: { beneficiaryLabel: 'Lives Supported' } } },
    },
  })
  layout!: Record<string, unknown>;
}
