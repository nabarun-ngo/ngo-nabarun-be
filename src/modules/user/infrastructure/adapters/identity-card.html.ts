import type { IdentityCardPdfInput } from '../../domain/ports/identity-card-pdf.port';

/** CR80 (ISO/IEC 7810 ID-1): 85.60 mm × 53.98 mm. */
export const IDENTITY_CARD_WIDTH_MM = '85.60mm';
export const IDENTITY_CARD_HEIGHT_MM = '53.98mm';

export function renderIdentityCardHtml(
  input: IdentityCardPdfInput,
  qrDataUrl: string,
): string {
  const org = escapeHtml(input.organisationName);
  const registration = input.organisationRegistrationNumber?.trim()
    ? `<div class="reg">Reg. No. ${escapeHtml(input.organisationRegistrationNumber.trim())}</div>`
    : '';
  const name = escapeHtml(input.displayName);
  const role = escapeHtml(input.roleLabel);
  const memberId = escapeHtml(input.uniqueMemberId);
  const initials = escapeHtml(input.initials.slice(0, 4));
  const logo = input.organisationLogoDataUrl
    ? `<img src="${escapeAttr(input.organisationLogoDataUrl)}" alt="" />`
    : escapeHtml((input.organisationName.trim()[0] || 'M').toUpperCase());
  const face = input.pictureDataUrl
    ? `<img src="${escapeAttr(input.pictureDataUrl)}" alt="" />`
    : initials;
  const contact = input.contactNumber
    ? `<div class="field">
        <div class="label">Contact</div>
        <div class="value">${escapeHtml(input.contactNumber)}</div>
      </div>`
    : '';
  const blood = input.bloodGroup
    ? `<div class="field">
        <div class="label">Blood group</div>
        <div class="value">${escapeHtml(input.bloodGroup)}</div>
      </div>`
    : '';

  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <style>${IDENTITY_CARD_CSS}</style>
</head>
<body>
  <article class="card">
    <div class="header">
      <div class="logo">${logo}</div>
      <div class="brand">
        <div class="org">${org}</div>
        ${registration}
      </div>
    </div>
    <div class="face">${face}</div>
    <div class="details">
      <div class="who">
        <div class="name">${name}</div>
        <div class="role">${role}</div>
      </div>
      <div class="meta">
        <div class="field">
          <div class="label">Member ID</div>
          <div class="value member-id">${memberId}</div>
        </div>
        ${blood}
      </div>
      ${contact}
    </div>
    <img class="qr" src="${escapeAttr(qrDataUrl)}" alt="" />
    <div class="qr-caption">Scan to check membership</div>
    <div class="footer">Valid only while membership is Active</div>
  </article>
</body>
</html>`;
}

const IDENTITY_CARD_CSS = `
  @page { size: ${IDENTITY_CARD_WIDTH_MM} ${IDENTITY_CARD_HEIGHT_MM}; margin: 0; }
  * { box-sizing: border-box; }
  html, body {
    margin: 0;
    width: ${IDENTITY_CARD_WIDTH_MM};
    height: ${IDENTITY_CARD_HEIGHT_MM};
    background: #ffffff;
    font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    -webkit-print-color-adjust: exact;
    print-color-adjust: exact;
  }
  .card {
    position: relative;
    width: ${IDENTITY_CARD_WIDTH_MM};
    height: ${IDENTITY_CARD_HEIGHT_MM};
    overflow: hidden;
    background: #ffffff;
  }
  .header {
    position: absolute;
    inset: 0 0 auto 0;
    height: 10.6mm;
    background: linear-gradient(90deg, #e8791f 0%, #f39c12 100%);
    color: #ffffff;
  }
  .logo {
    position: absolute;
    left: 2.1mm;
    top: 2.5mm;
    width: 5.6mm;
    height: 5.6mm;
    border-radius: 1.05mm;
    background: #ffffff;
    color: #e8791f;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 3.3mm;
    overflow: hidden;
  }
  .logo img {
    width: 4.6mm;
    height: 4.6mm;
    object-fit: contain;
  }
  .brand {
    position: absolute;
    left: 8.6mm;
    top: 1.6mm;
    right: 2.5mm;
  }
  .org {
    font-weight: 700;
    font-size: 2.45mm;
    line-height: 3.2mm;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .reg {
    margin-top: 0.3mm;
    font-size: 1.85mm;
    line-height: 2.4mm;
    font-weight: 600;
    letter-spacing: 0.04mm;
    opacity: 0.95;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .face {
    position: absolute;
    left: 3.5mm;
    top: 19.6mm;
    width: 12.7mm;
    height: 12.7mm;
    border-radius: 50%;
    border: 0.35mm solid #e8791f;
    background: #e8791f;
    color: #ffffff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 700;
    font-size: 3.5mm;
    overflow: hidden;
  }
  .face img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  .details {
    position: absolute;
    left: 19mm;
    top: 11.6mm;
    width: 36mm;
  }
  .who {
    margin-bottom: 1.5mm;
  }
  .name {
    color: #1e293b;
    font-weight: 700;
    font-size: 3.2mm;
    line-height: 3.6mm;
    max-height: 7.2mm;
    overflow: hidden;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
  }
  .role {
    margin-top: 0.7mm;
    color: #e8791f;
    font-weight: 700;
    font-size: 2.2mm;
    line-height: 2.6mm;
  }
  .meta {
    display: flex;
    gap: 2mm;
  }
  .field {
    flex: 1;
    min-width: 0;
  }
  .meta + .field,
  .field + .field {
    margin-top: 1.3mm;
  }
  .label {
    color: #64748b;
    font-size: 1.7mm;
    line-height: 2.1mm;
  }
  .value {
    color: #1e293b;
    font-weight: 700;
    font-size: 2.6mm;
    line-height: 3.2mm;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .member-id {
    font-family: ui-monospace, Consolas, 'Courier New', monospace;
    font-size: 2.8mm;
    letter-spacing: 0.04mm;
  }
  .qr {
    position: absolute;
    right: 2.8mm;
    top: 12.4mm;
    width: 18.3mm;
    height: 18.3mm;
  }
  .qr-caption {
    position: absolute;
    right: 1.4mm;
    top: 31.4mm;
    width: 21.1mm;
    color: #64748b;
    font-size: 1.7mm;
    text-align: center;
    line-height: 2.1mm;
  }
  .footer {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    height: 3.5mm;
    background: #2c3e50;
    color: #ffffff;
    font-size: 1.9mm;
    display: flex;
    align-items: center;
    justify-content: center;
  }
`;

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function escapeAttr(value: string): string {
  return escapeHtml(value).replace(/'/g, '&#39;');
}
