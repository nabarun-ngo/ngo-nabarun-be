import { Prisma } from '../../prisma/client';
import { Account } from '../../../../modules/finance/domain/aggregates/account/account.aggregate';
import { AccountOwnerType } from '../../../../modules/finance/domain/enums/account-owner-type.enum';
import { AccountStatus } from '../../../../modules/finance/domain/enums/account-status.enum';
import { AccountType } from '../../../../modules/finance/domain/enums/account-type.enum';
import { BankDetail } from '../../../../modules/finance/domain/value-objects/bank-detail.vo';
import { UPIDetail } from '../../../../modules/finance/domain/value-objects/upi-detail.vo';
import { MapperUtils } from './mapper-utils';
import { TransactionPrismaMapper } from './transaction-prisma.mapper';
import { AccountWithRelations } from '../repositories/account.prisma-repository';

export class AccountPrismaMapper {
  static toAccountDomain(p: AccountWithRelations | null): Account | null {
    if (!p) return null;

    return new Account(
      p.id,
      p.name,
      p.type as AccountType,
      p.ownerType as AccountOwnerType,
      p.currency,
      p.status as AccountStatus,
      MapperUtils.nullToUndefined(p.description),
      p.transactions?.map((t) => TransactionPrismaMapper.toTransactionDomain(t)!) ?? [],
      MapperUtils.nullToUndefined(p.accountHolderName),
      MapperUtils.nullToUndefined(p.accountHolderId),
      p.custodianUserIds ?? [],
      MapperUtils.nullToUndefined(p.activatedOn),
      AccountPrismaMapper.toBankDetail(p.bankInvestDetail),
      AccountPrismaMapper.toUpiDetails(p.upiDetailRows),
      p.createdAt,
      p.updatedAt,
    );
  }

  static toAccountCreatePersistence(domain: Account): Prisma.AccountUncheckedCreateInput {
    return {
      id: domain.id,
      name: domain.name,
      type: domain.type,
      ownerType: domain.ownerType,
      currency: domain.currency,
      status: domain.status,
      balance: 0,
      description: MapperUtils.undefinedToNull(domain.description),
      createdAt: domain.createdAt,
      updatedAt: domain.updatedAt,
      accountHolderId: domain.accountHolderId ?? null,
      accountHolderName: domain.accountHolderName ?? null,
      custodianUserIds: domain.custodianUserIds,
      activatedOn: domain.activatedOn ?? null,
      bankInvestDetail: domain.bankDetail
        ? { create: AccountPrismaMapper.fromBankInvestDetail(domain.bankDetail) }
        : undefined,
      upiDetailRows: domain.upiDetails?.length
        ? { create: domain.upiDetails.map((detail) => AccountPrismaMapper.fromUpiDetail(detail)) }
        : undefined,
    };
  }

  static toAccountUpdatePersistence(
    domain: Account,
    options: { replaceUpiDetails?: boolean } = {},
  ): Prisma.AccountUncheckedUpdateInput {
    const data: Prisma.AccountUncheckedUpdateInput = {
      name: domain.name,
      status: domain.status,
      description: MapperUtils.undefinedToNull(domain.description),
      updatedAt: new Date(),
      activatedOn: domain.activatedOn ?? null,
      currency: domain.currency,
      type: domain.type,
      ownerType: domain.ownerType,
      accountHolderId: domain.accountHolderId ?? null,
      accountHolderName: domain.accountHolderName ?? null,
      custodianUserIds: domain.custodianUserIds,
    };

    if (domain.bankDetail) {
      data.bankInvestDetail = {
        upsert: {
          create: AccountPrismaMapper.fromBankInvestDetail(domain.bankDetail),
          update: AccountPrismaMapper.fromBankInvestDetail(domain.bankDetail),
        },
      };
    }

    if (options.replaceUpiDetails) {
      data.upiDetailRows = {
        deleteMany: {},
        create: domain.upiDetails?.map((detail) => AccountPrismaMapper.fromUpiDetail(detail)) ?? [],
      };
    }

    return data;
  }

  private static toBankDetail(
    row: Prisma.AccountBankInvestDetailGetPayload<object> | null | undefined,
  ): BankDetail | undefined {
    if (!row) return undefined;
    return new BankDetail(
      MapperUtils.nullToUndefined(row.bankAccountHolderName),
      MapperUtils.nullToUndefined(row.bankName),
      MapperUtils.nullToUndefined(row.bankBranch),
      MapperUtils.nullToUndefined(row.bankAccountNumber),
      MapperUtils.nullToUndefined(row.bankAccountType),
      MapperUtils.nullToUndefined(row.IFSCNumber),
      row.maturityDate ? row.maturityDate.toISOString().slice(0, 10) : undefined,
      row.maturityAmount != null ? Number(row.maturityAmount) : undefined,
      row.investmentAmount != null ? Number(row.investmentAmount) : undefined,
      MapperUtils.nullToUndefined(row.sourceAccountId),
      MapperUtils.nullToUndefined(row.dematId),
      row.interestRate != null ? Number(row.interestRate) : undefined,
      MapperUtils.nullToUndefined(row.interestPayingTerm),
    );
  }

  private static fromBankInvestDetail(
    detail: BankDetail,
  ): Prisma.AccountBankInvestDetailUncheckedCreateWithoutAccountInput {
    return {
      bankAccountHolderName: detail.bankAccountHolderName ?? null,
      bankName: detail.bankName ?? null,
      bankBranch: detail.bankBranch ?? null,
      bankAccountNumber: detail.bankAccountNumber ?? null,
      bankAccountType: detail.bankAccountType ?? null,
      IFSCNumber: detail.IFSCNumber ?? null,
      maturityDate: detail.maturityDate ? new Date(detail.maturityDate) : null,
      maturityAmount: detail.maturityAmount ?? null,
      investmentAmount: detail.investmentAmount ?? null,
      sourceAccountId: detail.sourceAccountId ?? null,
      dematId: detail.dematId ?? null,
      interestRate: detail.interestRate ?? null,
      interestPayingTerm: detail.interestPayingTerm ?? null,
    };
  }

  private static toUpiDetails(
    rows: Prisma.AccountUpiDetailGetPayload<object>[] | null | undefined,
  ): UPIDetail[] | undefined {
    if (!rows?.length) return undefined;
    return rows.map((row) => new UPIDetail(
      row.id,
      MapperUtils.nullToUndefined(row.payeeName),
      MapperUtils.nullToUndefined(row.upiId),
      MapperUtils.nullToUndefined(row.mobileNumber),
      MapperUtils.nullToUndefined(row.qrData),
      MapperUtils.nullToUndefined(row.label),
      row.isPrimary,
    ));
  }

  private static fromUpiDetail(
    detail: UPIDetail,
  ): Prisma.AccountUpiDetailUncheckedCreateWithoutAccountInput {
    const uuidPattern = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return {
      ...(detail.id && uuidPattern.test(detail.id) ? { id: detail.id } : {}),
      payeeName: detail.payeeName ?? null,
      upiId: detail.upiId ?? null,
      mobileNumber: detail.mobileNumber ?? null,
      qrData: detail.qrData ?? null,
      label: detail.label ?? null,
      isPrimary: detail.isPrimary ?? false,
    };
  }
}
