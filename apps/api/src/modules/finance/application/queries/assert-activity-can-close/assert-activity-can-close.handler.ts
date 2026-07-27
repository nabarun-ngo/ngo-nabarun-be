import { Inject, Injectable } from '@nestjs/common';
import { IQueryHandler, QueryHandler } from '@nestjs/cqrs';
import { BusinessException } from '@nabarun-ngo/nestjs-shared-core';
import { ExpenseStatus } from '../../../domain/enums/expense.enum';
import { DonationStatus } from '../../../domain/enums/donation-status.enum';
import { IExpenseRepository } from '../../../domain/repositories/expense.repository';
import { IDonationRepository } from '../../../domain/repositories/donation.repository';
import { AssertActivityCanCloseQuery } from './assert-activity-can-close.query';

@QueryHandler(AssertActivityCanCloseQuery)
@Injectable()
export class AssertActivityCanCloseHandler implements IQueryHandler<AssertActivityCanCloseQuery, void> {
  constructor(
    @Inject(IExpenseRepository) private readonly expenseRepository: IExpenseRepository,
    @Inject(IDonationRepository) private readonly donationRepository: IDonationRepository,
  ) {}

  async execute(query: AssertActivityCanCloseQuery): Promise<void> {
    const expenses = await this.expenseRepository.findAll({ expenseRefId: query.activityId });
    const allowedExpense = [ExpenseStatus.SETTLED, ExpenseStatus.REJECTED];
    if (expenses.some((e) => !allowedExpense.includes(e.status))) {
      throw new BusinessException('Cannot close activity because there are unsettled expenses.');
    }

    const donations = await this.donationRepository.findAll({ forEventId: query.activityId });
    const allowedDonation = [DonationStatus.PAID, DonationStatus.CANCELLED];
    if (donations.some((d) => !allowedDonation.includes(d.status))) {
      throw new BusinessException('Cannot close activity because there are unsettled donations.');
    }
  }
}
