import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { MAX_UPI_DETAILS_PER_ACCOUNT } from '../config/account-type.config';

export class UPIDetail {
  constructor(
    public id?: string,
    public payeeName?: string,
    public upiId?: string,
    public mobileNumber?: string,
    public qrData?: string,
    public label?: string,
    public isPrimary?: boolean,
  ) {}
}

export function normalizeUpiDetails(details: UPIDetail[] | undefined): UPIDetail[] | undefined {
  if (details === undefined) return undefined;
  if (details.length === 0) return [];
  if (details.length > MAX_UPI_DETAILS_PER_ACCOUNT) {
    throw new BusinessException(`At most ${MAX_UPI_DETAILS_PER_ACCOUNT} UPI IDs are allowed per account`);
  }

  const ids = new Set<string>();
  const upiIds = new Set<string>();
  const normalized = details.map((detail, index) => {
    const id = detail.id?.trim() || `upi-${index + 1}`;
    if (ids.has(id)) throw new BusinessException('Duplicate UPI entry id');
    ids.add(id);

    const upiId = detail.upiId?.trim();
    if (upiId) {
      const key = upiId.toLowerCase();
      if (upiIds.has(key)) throw new BusinessException('Duplicate UPI Id');
      upiIds.add(key);
    }

    return new UPIDetail(
      id,
      detail.payeeName?.trim(),
      upiId,
      detail.mobileNumber?.trim(),
      detail.qrData?.trim(),
      detail.label?.trim(),
      detail.isPrimary === true,
    );
  });

  let primaryCount = normalized.filter((d) => d.isPrimary).length;
  if (primaryCount === 0 && normalized.length === 1) {
    normalized[0] = new UPIDetail(
      normalized[0].id,
      normalized[0].payeeName,
      normalized[0].upiId,
      normalized[0].mobileNumber,
      normalized[0].qrData,
      normalized[0].label,
      true,
    );
    primaryCount = 1;
  } else if (primaryCount === 0 && normalized.length > 1) {
    normalized[0] = new UPIDetail(
      normalized[0].id,
      normalized[0].payeeName,
      normalized[0].upiId,
      normalized[0].mobileNumber,
      normalized[0].qrData,
      normalized[0].label,
      true,
    );
    primaryCount = 1;
  }

  if (primaryCount !== 1) {
    throw new BusinessException('Exactly one primary UPI Id is required');
  }

  return normalized;
}

export function upiDetailFromLegacy(detail: UPIDetail): UPIDetail[] {
  return normalizeUpiDetails([new UPIDetail(
    detail.id ?? 'primary',
    detail.payeeName,
    detail.upiId,
    detail.mobileNumber,
    detail.qrData,
    detail.label,
    true,
  )]) ?? [];
}

export function getPrimaryUpiDetail(details: UPIDetail[] | undefined): UPIDetail | undefined {
  if (!details?.length) return undefined;
  return details.find((d) => d.isPrimary) ?? details[0];
}
