import { Controller, Get, Header, Headers, Param, Res } from '@nestjs/common';
import type { Response } from 'express';
import { QueryBus } from '@nestjs/cqrs';
import { ApiOperation, ApiProduces, ApiTags } from '@nestjs/swagger';
import {
  IgnoreCaptcha,
  Public,
  StrictThrottle,
} from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoResponse, ApiKeyParam } from '@nabarun-ngo/nestjs-shared-core';
import {
  IdentityCardVerificationDto,
  IdentityCardVerificationOutcome,
} from '../../application/dtos/identity-card.dto';
import { VerifyIdentityCardQuery } from '../../application/queries/verify-identity-card/verify-identity-card.query';

@ApiTags('Identity cards')
@Controller('public/identity-cards')
@Public()
@IgnoreCaptcha()
export class PublicIdentityCardController {
  constructor(private readonly queryBus: QueryBus) {}

  @Get(':uniqueMemberId')
  @StrictThrottle({ limit: 30, ttlMs: 60_000 })
  @ApiOperation({ summary: 'Public membership check for a printed identity card' })
  @ApiKeyParam('uniqueMemberId', 'NM24128765', 'Printed lifetime membership number')
  @ApiProduces('text/html', 'application/json')
  @ApiAutoResponse(IdentityCardVerificationDto)
  @Header('Cache-Control', 'no-store')
  async verify(
    @Param('uniqueMemberId') uniqueMemberId: string,
    @Headers('accept') accept?: string,
    @Res({ passthrough: true }) res?: Response,
  ): Promise<IdentityCardVerificationDto | string> {
    const result = await this.queryBus.execute(
      new VerifyIdentityCardQuery(uniqueMemberId),
    );
    if ((accept ?? '').toLowerCase().includes('application/json')) {
      res?.type('application/json');
      return result;
    }
    res?.type('text/html');
    return renderVerifyHtml(result);
  }
}

function renderVerifyHtml(result: IdentityCardVerificationDto): string {
  const title =
    result.outcome === IdentityCardVerificationOutcome.VALID
      ? 'Valid membership'
      : result.outcome === IdentityCardVerificationOutcome.INVALID
        ? 'Membership is not active'
        : 'Card not recognised';
  const name = escapeHtml(result.displayName ?? '');
  const memberId = escapeHtml(result.uniqueMemberId ?? '');
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1" />
  <title>${escapeHtml(title)}</title>
  <style>
    body { margin: 0; font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
      color: #334155; background: #f1f5f9; }
    main { max-width: 22rem; margin: 2.5rem auto; padding: 1.5rem;
      background: #fff; border-radius: 16px; border: 1px solid #e2e8f0; }
    h1 { font-size: 1.25rem; margin: 0 0 0.5rem; color: #2c3e50; }
    p { margin: 0.35rem 0; color: #64748b; }
    .id { font-family: ui-monospace, Consolas, monospace; color: #e74c3c; font-size: 1.05rem; }
  </style>
</head>
<body>
  <main>
    <h1>${escapeHtml(title)}</h1>
    ${memberId ? `<p class="id">${memberId}</p>` : ''}
    ${name ? `<p>${name}</p>` : ''}
    <p>Treat this card as membership only when this check says valid.</p>
  </main>
</body>
</html>`;
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}
