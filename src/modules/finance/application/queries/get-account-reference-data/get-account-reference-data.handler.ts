import { Inject, Injectable, Optional } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { IFinanceReferenceDataPort } from '../../ports/finance-reference-data.port';
import { AccountRefDataDto } from '../../dtos/account.dto';
import { GetAccountReferenceDataQuery } from './get-account-reference-data.query';

@QueryHandler(GetAccountReferenceDataQuery)
@Injectable()
export class GetAccountReferenceDataHandler implements IQueryHandler<GetAccountReferenceDataQuery, AccountRefDataDto> {
  constructor(@Optional() @Inject(IFinanceReferenceDataPort) private readonly port: IFinanceReferenceDataPort) {}

  async execute(): Promise<AccountRefDataDto> {
    if (!this.port) {
      return {};
    }
    const data = await this.port.getAccountReferenceData();
    return {
      accountStatuses: data.accountStatuses,
      accountTypes: data.accountTypes,
      ownerTypes: data.ownerTypes,
      bankAccountTypes: data.bankAccountTypes,
      investmentTypes: data.investmentTypes,
      interestPayingTerms: data.interestPayingTerms,
      transferReferenceTypes: data.transferReferenceTypes,
      transferMatrix: data.transferMatrix,
      transactionTypes: data.transactionTypes,
      transactionStatuses: data.transactionStatuses,
      transactionRefTypes: data.transactionRefTypes,
      accountStatusGroups: data.accountStatusGroups,
    };
  }
}
