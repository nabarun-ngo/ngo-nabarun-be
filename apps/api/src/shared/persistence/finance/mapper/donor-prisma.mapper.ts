import { Donor } from '../../../../modules/finance/domain/aggregates/donor/donor.aggregate';
import { DonorStatus } from '../../../../modules/finance/domain/enums/donor-status.enum';
import { DonorType } from '../../../../modules/finance/domain/enums/donor-type.enum';
import { Prisma } from '../../prisma/client';
import { MapperUtils } from './mapper-utils';

type DonorRow = {
  id: string;
  type: string;
  status: string;
  preferredAmount: Prisma.Decimal | null;
  statusEndDate: Date | null;
  fullName: string | null;
  email: string | null;
  phoneCode: string | null;
  phoneNumber: string | null;
  userProfileId: string | null;
  createdAt: Date;
  updatedAt: Date;
  version: number;
  deletedAt: Date | null;
  userProfile?: {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    phoneNumbers?: { phoneCode: string; phoneNumber: string; isPrimary: boolean }[];
  } | null;
};

export class DonorPrismaMapper {
  static toDomain(p: DonorRow | null): Donor | null {
    if (!p) return null;
    return new Donor(
      p.id,
      p.type as DonorType,
      p.status as DonorStatus,
      p.preferredAmount != null ? Number(p.preferredAmount) : undefined,
      MapperUtils.nullToUndefined(p.statusEndDate),
      MapperUtils.nullToUndefined(p.fullName),
      MapperUtils.nullToUndefined(p.email),
      MapperUtils.nullToUndefined(p.phoneCode),
      MapperUtils.nullToUndefined(p.phoneNumber),
      MapperUtils.nullToUndefined(p.userProfileId),
      p.createdAt,
      p.updatedAt,
    );
  }

  static toCreateInput(domain: Donor): Prisma.DonorUncheckedCreateInput {
    return {
      id: domain.id,
      type: domain.type,
      status: domain.status,
      preferredAmount: MapperUtils.undefinedToNull(domain.preferredAmount),
      statusEndDate: MapperUtils.undefinedToNull(domain.statusEndDate),
      fullName: MapperUtils.undefinedToNull(domain.fullName),
      email: MapperUtils.undefinedToNull(domain.email),
      phoneCode: MapperUtils.undefinedToNull(domain.phoneCode),
      phoneNumber: MapperUtils.undefinedToNull(domain.phoneNumber),
      userProfileId: MapperUtils.undefinedToNull(domain.userProfileId),
    };
  }

  static toUpdateInput(domain: Donor): Prisma.DonorUncheckedUpdateInput {
    return {
      type: domain.type,
      status: domain.status,
      preferredAmount: MapperUtils.undefinedToNull(domain.preferredAmount),
      statusEndDate: domain.statusEndDate === undefined ? undefined : domain.statusEndDate,
      fullName: MapperUtils.undefinedToNull(domain.fullName),
      email: MapperUtils.undefinedToNull(domain.email),
      phoneCode: MapperUtils.undefinedToNull(domain.phoneCode),
      phoneNumber: MapperUtils.undefinedToNull(domain.phoneNumber),
      userProfileId: MapperUtils.undefinedToNull(domain.userProfileId),
      version: { increment: 1 },
    };
  }

  static toUniqueWhere(id: string): Prisma.DonorWhereUniqueInput {
    return { id };
  }
}
