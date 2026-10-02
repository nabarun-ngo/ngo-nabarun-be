import { Inject, Injectable } from '@nestjs/common';
import { IIfscLookupPort } from '../ports/ifsc-lookup.port';
import {
  assertBankNameBranchMatchesLookup,
  assertIfscFormat,
  BankDetailInput,
} from '../../domain/validation/account-detail.validation';
import { normalizeIfsc } from '../../domain/validation/ifsc.validation';

@Injectable()
export class AccountBankIfscValidationService {
  constructor(@Inject(IIfscLookupPort) private readonly ifscLookup: IIfscLookupPort) {}

  async assertBankDetailIfscIntegrity(bank: BankDetailInput | undefined): Promise<void> {
    if (!bank?.IFSCNumber?.trim()) {
      return;
    }

    assertIfscFormat(bank.IFSCNumber);
    const lookup = await this.ifscLookup.lookup(normalizeIfsc(bank.IFSCNumber));
    assertBankNameBranchMatchesLookup(bank, lookup);
  }
}
