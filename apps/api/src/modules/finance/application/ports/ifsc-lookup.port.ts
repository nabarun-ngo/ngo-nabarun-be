export interface IfscLookupResult {
  ifsc: string;
  bankName: string;
  branch: string;
}

export abstract class IIfscLookupPort {
  abstract lookup(ifsc: string): Promise<IfscLookupResult>;
}
