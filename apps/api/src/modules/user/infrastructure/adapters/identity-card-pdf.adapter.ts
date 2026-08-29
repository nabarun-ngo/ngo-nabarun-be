import { Injectable } from '@nestjs/common';
import * as QRCode from 'qrcode';
import type {
  IIdentityCardPdfPort,
  IdentityCardPdfInput,
} from '../../domain/ports/identity-card-pdf.port';
import {
  IDENTITY_CARD_HEIGHT_MM,
  IDENTITY_CARD_WIDTH_MM,
  renderIdentityCardHtml,
} from './identity-card.html';

const DATA_IMAGE = /^data:image\/(jpeg|jpg|png|webp);base64,/i;

export interface IdentityCardPrintFiles {
  pdf: Buffer;
  png: Buffer;
}

/** Renders the CR80 card without HTTP. Used by print and by local preview tests. */
export async function printIdentityCard(
  input: IdentityCardPdfInput,
): Promise<IdentityCardPrintFiles> {
  const qrDataUrl = await QRCode.toDataURL(input.verifyUrl, {
    type: 'image/png',
    width: 240,
    margin: 1,
    errorCorrectionLevel: 'M',
  });
  const html = renderIdentityCardHtml(
    {
      ...input,
      pictureDataUrl: usableDataImage(input.pictureDataUrl),
      organisationLogoDataUrl: usableDataImage(input.organisationLogoDataUrl),
    },
    qrDataUrl,
  );

  const puppeteer = await import('puppeteer');
  const browser = await puppeteer.launch({
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox'],
  });
  try {
    const page = await browser.newPage();
    await page.setContent(html, { waitUntil: 'load' });
    const pdf = await page.pdf({
      width: IDENTITY_CARD_WIDTH_MM,
      height: IDENTITY_CARD_HEIGHT_MM,
      printBackground: true,
      margin: { top: 0, right: 0, bottom: 0, left: 0 },
    });
    const card = await page.$('.card');
    if (!card) throw new Error('Identity card root .card was not rendered');
    const png = await card.screenshot({ type: 'png' });
    return { pdf: Buffer.from(pdf), png: Buffer.from(png) };
  } finally {
    await browser.close();
  }
}

@Injectable()
export class IdentityCardPdfAdapter implements IIdentityCardPdfPort {
  async render(input: IdentityCardPdfInput): Promise<Buffer> {
    const { pdf } = await printIdentityCard(input);
    return pdf;
  }
}

function usableDataImage(value?: string): string | undefined {
  const raw = value?.trim();
  return raw && DATA_IMAGE.test(raw) ? raw : undefined;
}
