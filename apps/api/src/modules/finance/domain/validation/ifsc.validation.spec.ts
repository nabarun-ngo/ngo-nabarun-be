import { assertValidIfscFormat, isValidIfscFormat, normalizeIfsc } from './ifsc.validation';

describe('ifsc.validation', () => {
  it('normalizes IFSC to uppercase trimmed', () => {
    expect(normalizeIfsc(' hdfc0000001 ')).toBe('HDFC0000001');
  });

  it('accepts valid IFSC format', () => {
    expect(isValidIfscFormat('HDFC0000001')).toBe(true);
    expect(assertValidIfscFormat('stdb0001234')).toBe('STDB0001234');
  });

  it('rejects invalid IFSC format', () => {
    expect(isValidIfscFormat('HDFC00001')).toBe(false);
    expect(isValidIfscFormat('12345678901')).toBe(false);
    expect(() => assertValidIfscFormat('INVALID')).toThrow('Invalid IFSC format');
  });
});
