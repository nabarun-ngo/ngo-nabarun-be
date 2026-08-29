import { Inject, Injectable } from '@nestjs/common';
import { IUserRepository } from '../../domain/repositories/user.repository';
import { UserNotFoundError } from '../../domain/errors/user.errors';
import { IdentityCardIssuePolicy } from '../../domain/policies/identity-card-issue.policy';
import { IIdentityCardPdfPort } from '../../domain/ports/identity-card-pdf.port';
import type { PhoneNumber } from '../../domain/value-objects/phone-number.vo';
import { USER_OPTIONS } from '../../infrastructure/user-options.token';
import type { UserModuleOptions } from '../../user.schema';
import { IdentityCardPdfResult } from '../dtos/identity-card.dto';

const DATA_IMAGE = /^data:image\/(jpeg|jpg|png|webp);base64,/i;

@Injectable()
export class IdentityCardPdfService {
  constructor(
    @Inject(IUserRepository) private readonly users: IUserRepository,
    @Inject(IIdentityCardPdfPort) private readonly pdf: IIdentityCardPdfPort,
    @Inject(USER_OPTIONS) private readonly options: UserModuleOptions,
  ) {}

  async renderForUser(userId: string, pictureDataUrl?: string): Promise<IdentityCardPdfResult> {
    const user = await this.users.findById(userId);
    if (!user) throw new UserNotFoundError(userId);

    IdentityCardIssuePolicy.assertCanPrint(user);

    if (!user.uniqueMemberId) {
      const allocated = await this.users.allocateNextUniqueMemberId(user.createdAt);
      user.assignUniqueMemberId(allocated);
      await this.users.update(user.id, user);
    }

    const uniqueMemberId = user.uniqueMemberId!;
    const picture = firstDataImage(pictureDataUrl, user.picture);
    const origin = this.options.publicApiUrl.replace(/\/$/, '');
    const verifyUrl = `${origin}/public/identity-cards/${encodeURIComponent(uniqueMemberId)}`;
    const displayName = [user.title, user.fullName].filter(Boolean).join(' ').trim();

    const buffer = await this.pdf.render({
      organisationName: this.options.organisationName,
      organisationRegistrationNumber: this.options.organisationRegistrationNumber,
      organisationLogoDataUrl: firstDataImage(this.options.organisationLogoDataUrl),
      displayName,
      roleLabel: roleLabel(user.roleKeys),
      initials: user.initials,
      uniqueMemberId,
      contactNumber: formatPhone(user.primaryPhone) ?? formatPhone(user.secondaryPhone),
      bloodGroup: user.bloodGroup,
      verifyUrl,
      pictureDataUrl: picture,
    });

    return {
      buffer,
      fileName: `identity-card-${uniqueMemberId}.pdf`,
      uniqueMemberId,
    };
  }
}

/** Highest-signal role for print. Falls back to `Member` so the line is never blank. */
function roleLabel(roleKeys: string[]): string {
  const key = roleKeys.find((value) => value?.trim());
  if (!key) return 'Member';
  return key
    .trim()
    .toLowerCase()
    .split(/[_\-\s]+/)
    .filter(Boolean)
    .map((word) => word[0].toUpperCase() + word.slice(1))
    .join(' ');
}

function formatPhone(phone?: PhoneNumber): string | undefined {
  if (!phone) return undefined;
  const code = phone.phoneCode.replace(/^\+?/, '+');
  return `${code} ${phone.phoneNumber}`.trim();
}

function firstDataImage(...values: Array<string | undefined>): string | undefined {
  for (const value of values) {
    const trimmed = value?.trim();
    if (trimmed && DATA_IMAGE.test(trimmed)) {
      return trimmed;
    }
  }
  return undefined;
}
