/**
 * Lifetime membership number printed on the identity card.
 * Format: `NM` + YYmm + 3-digit sequence + 1 Luhn check digit (10 characters).
 */
export class UniqueMemberId {
  static readonly PREFIX = 'NM';
  static readonly MAX_SEQUENCE = 999;

  static yymmUtc(at: Date): string {
    const yy = String(at.getUTCFullYear()).slice(-2);
    const mm = String(at.getUTCMonth() + 1).padStart(2, '0');
    return `${yy}${mm}`;
  }

  /** Luhn check digit for a numeric payload (rightmost digit of payload is not doubled). */
  static luhnCheckDigit(digits: string): string {
    if (!/^\d+$/.test(digits)) {
      throw new Error('Luhn payload must be numeric');
    }
    let sum = 0;
    let double = false;
    for (let i = digits.length - 1; i >= 0; i--) {
      let n = Number(digits[i]);
      if (double) {
        n *= 2;
        if (n > 9) n -= 9;
      }
      sum += n;
      double = !double;
    }
    return String((10 - (sum % 10)) % 10);
  }

  static compose(yymm: string, sequence: number): string {
    if (!/^\d{4}$/.test(yymm)) {
      throw new Error('YYmm must be four digits');
    }
    if (!Number.isInteger(sequence) || sequence < 1 || sequence > UniqueMemberId.MAX_SEQUENCE) {
      throw new Error(`Sequence must be 1–${UniqueMemberId.MAX_SEQUENCE} for ${yymm}`);
    }
    const seq = String(sequence).padStart(3, '0');
    const body = `${yymm}${seq}`;
    return `${UniqueMemberId.PREFIX}${body}${UniqueMemberId.luhnCheckDigit(body)}`;
  }

  static parseSequence(id: string, yymm: string): number | null {
    if (!id || !yymm) return null;
    const match = id.match(new RegExp(`^${UniqueMemberId.PREFIX}${yymm}(\\d{3})\\d$`));
    return match ? Number(match[1]) : null;
  }

  static isWellFormed(id: string): boolean {
    const value = id?.trim() ?? '';
    if (!new RegExp(`^${UniqueMemberId.PREFIX}\\d{8}$`).test(value)) {
      return false;
    }
    const body = value.slice(2, 9);
    const check = value.slice(9);
    return UniqueMemberId.luhnCheckDigit(body) === check;
  }
}
