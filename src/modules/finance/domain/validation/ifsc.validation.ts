const IFSC_FORMAT = /^[A-Z]{4}0[A-Z0-9]{6}$/;

export function normalizeIfsc(value: string): string {
  return value.trim().toUpperCase();
}

export function isValidIfscFormat(value: string): boolean {
  return IFSC_FORMAT.test(normalizeIfsc(value));
}

export function assertValidIfscFormat(value: string): string {
  const normalized = normalizeIfsc(value);
  if (!IFSC_FORMAT.test(normalized)) {
    throw new Error('Invalid IFSC format');
  }
  return normalized;
}
