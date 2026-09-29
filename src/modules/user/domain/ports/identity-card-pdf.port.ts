export const IIdentityCardPdfPort = Symbol('IIdentityCardPdfPort');

export interface IdentityCardPdfInput {
  organisationName: string;
  /** Society/NGO registration number printed under the organisation name. */
  organisationRegistrationNumber?: string;
  displayName: string;
  /** Role line printed under the name, e.g. `Member`. */
  roleLabel: string;
  initials: string;
  uniqueMemberId: string;
  /** Already formatted for print, e.g. `+91 90000 00000`. Omitted when unknown. */
  contactNumber?: string;
  /** ABO/Rh group, e.g. `B+`. Omitted when unknown. */
  bloodGroup?: string;
  verifyUrl: string;
  /** data:image/...;base64,... only. Remote http(s) URLs are ignored. */
  pictureDataUrl?: string;
  /** Organisation mark in the header. Same embedding rules as the face photo. */
  organisationLogoDataUrl?: string;
}

export interface IIdentityCardPdfPort {
  render(input: IdentityCardPdfInput): Promise<Buffer>;
}
