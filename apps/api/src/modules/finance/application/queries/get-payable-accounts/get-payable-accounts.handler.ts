import { Inject, Injectable, Optional } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { IAccountRepository } from '../../../domain/repositories/account.repository';
import { AccountOwnerType } from '../../../domain/enums/account-owner-type.enum';
import { AccountStatus } from '../../../domain/enums/account-status.enum';
import { AccountType } from '../../../domain/enums/account-type.enum';
import { AccountDetailDto } from '../../dtos/account.dto';
import { AccountMapper } from '../../mappers/account.mapper';
import { IFinanceReferenceDataPort } from '../../ports/finance-reference-data.port';
import {
  DEFAULT_TRANSFER_MATRIX,
  resolveTransferToAccountTypes,
} from '../../commands/transfer-amount/transfer-matrix';
import {
  GetPayableAccountsQuery,
  type PayableAccountReference,
} from './get-payable-accounts.query';

@QueryHandler(GetPayableAccountsQuery)
@Injectable()
export class GetPayableAccountsHandler implements IQueryHandler<GetPayableAccountsQuery, AccountDetailDto[]> {
  constructor(
    @Inject(IAccountRepository) private readonly repo: IAccountRepository,
    @Optional() @Inject(IFinanceReferenceDataPort) private readonly refDataPort?: IFinanceReferenceDataPort,
  ) {}

  async execute(query: GetPayableAccountsQuery): Promise<AccountDetailDto[]> {
    if (query.purpose === 'EARNING_INTEREST') {
      const accounts = await this.repo.findAll({
        type: [AccountType.BANK, AccountType.INVESTMENT],
        status: [AccountStatus.ACTIVE],
        includeBalance: false,
      });
      return accounts.map((a) => AccountMapper.toDto(a, { includeBankDetail: true, includeUpiDetail: true }));
    }

    if (query.purpose === 'INVESTMENT_FUNDING') {
      const accounts = await this.repo.findAll({
        type: [AccountType.BANK],
        status: [AccountStatus.ACTIVE],
        includeBalance: true,
      });
      return accounts.map((a) => AccountMapper.toDto(a, { includeBankDetail: true, includeUpiDetail: true }));
    }

    if (!query.reference) {
      const accounts = await this.repo.findAll({
        type: [AccountType.BANK],
        ownerType: [AccountOwnerType.ORG],
        status: [AccountStatus.ACTIVE],
        includeBalance: false,
      });
      return accounts.map((a) => AccountMapper.toDto(a, { includeBankDetail: true, includeUpiDetail: true }));
    }

    let fromType: AccountType | undefined;
    if (query.fromAccountId) {
      const fromAccount = await this.repo.findById(query.fromAccountId);
      if (!fromAccount) {
        throw new BusinessException(`Account not found with id: ${query.fromAccountId}`);
      }
      fromType = fromAccount.type;
      if (fromType === AccountType.INVESTMENT) {
        return [];
      }
    }

    const filters = await this.resolveTransferFilters(query.reference, fromType);
    if (!filters) {
      return [];
    }

    const accounts = await this.repo.findAll({
      ...filters,
      status: [AccountStatus.ACTIVE],
      includeBalance: false,
    });

    return accounts
      .filter((account) => account.id !== query.fromAccountId)
      .map((a) => AccountMapper.toDto(a, { includeBankDetail: true, includeUpiDetail: true }));
  }

  private async resolveTransferFilters(
    reference: PayableAccountReference,
    fromType?: AccountType,
  ): Promise<{ type: AccountType[]; ownerType: AccountOwnerType[] } | null> {
    const accountRef = this.refDataPort
      ? await this.refDataPort.getAccountReferenceData()
      : undefined;
    const matrix = accountRef?.transferMatrix?.length
      ? accountRef.transferMatrix
      : DEFAULT_TRANSFER_MATRIX;

    const toTypes = resolveTransferToAccountTypes(matrix, fromType, reference);
    if (!toTypes?.length) {
      return null;
    }

    return {
      type: toTypes as AccountType[],
      ownerType: [],
    };
  }
}
