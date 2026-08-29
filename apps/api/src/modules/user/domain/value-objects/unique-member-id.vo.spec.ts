import { UniqueMemberId } from './unique-member-id.vo';

describe('UniqueMemberId', () => {
  it('formats NM + YYmm + sequence + Luhn digit', () => {
    const id = UniqueMemberId.compose('2412', 876);
    expect(id).toMatch(/^NM2412876\d$/);
    expect(id).toHaveLength(10);
    expect(UniqueMemberId.isWellFormed(id)).toBe(true);
  });

  it('uses UTC year-month', () => {
    expect(UniqueMemberId.yymmUtc(new Date(Date.UTC(2024, 11, 15)))).toBe('2412');
  });

  it('rejects sequence 0 and 1000', () => {
    expect(() => UniqueMemberId.compose('2412', 0)).toThrow(/Sequence/);
    expect(() => UniqueMemberId.compose('2412', 1000)).toThrow(/Sequence/);
  });

  it('detects a tampered check digit', () => {
    const id = UniqueMemberId.compose('2412', 1);
    const broken = `${id.slice(0, 9)}${id[9] === '0' ? '1' : '0'}`;
    expect(UniqueMemberId.isWellFormed(broken)).toBe(false);
  });

  it('parses sequence for a month prefix', () => {
    const id = UniqueMemberId.compose('2608', 12);
    expect(UniqueMemberId.parseSequence(id, '2608')).toBe(12);
    expect(UniqueMemberId.parseSequence(id, '2607')).toBeNull();
  });

  it('rejects malformed strings', () => {
    expect(UniqueMemberId.isWellFormed('NM2412876')).toBe(false);
    expect(UniqueMemberId.isWellFormed('XX24128765')).toBe(false);
    expect(UniqueMemberId.isWellFormed('')).toBe(false);
  });
});
