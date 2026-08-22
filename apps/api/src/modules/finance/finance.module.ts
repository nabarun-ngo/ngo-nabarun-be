import { DynamicModule, FactoryProvider, Module, ModuleMetadata, Provider } from '@nestjs/common';
import { HttpModule } from '@nestjs/axios';
import { CqrsModule } from '@nestjs/cqrs';
import { createRequiredPortsGuard } from '@nabarun-ngo/nestjs-shared-core';
import { FinanceModuleInput, FinanceModuleOptions, FinanceModuleOptionsSchema } from './finance.schema';
import { FINANCE_OPTIONS } from './infrastructure/finance-options.token';
import { IFinanceReferenceDataPort } from './application/ports/finance-reference-data.port';
import { IAccountRepository } from './domain/repositories/account.repository';
import { IDonationRepository } from './domain/repositories/donation.repository';
import { IDonorRepository } from './domain/repositories/donor.repository';
import { IExpenseRepository } from './domain/repositories/expense.repository';
import { IEarningRepository } from './domain/repositories/earning.repository';
import { ITransactionRepository } from './domain/repositories/transaction.repository';
import { AccountPrismaRepository } from '../../shared/persistence/finance/repositories/account.prisma-repository';
import { DonationPrismaRepository } from '../../shared/persistence/finance/repositories/donation.prisma-repository';
import { DonorPrismaRepository } from '../../shared/persistence/finance/repositories/donor.prisma-repository';
import { ExpensePrismaRepository } from '../../shared/persistence/finance/repositories/expense.prisma-repository';
import { EarningPrismaRepository } from '../../shared/persistence/finance/repositories/earning.prisma-repository';
import { TransactionPrismaRepository } from '../../shared/persistence/finance/repositories/transaction.prisma-repository';
import { FinanceDmsAdapter } from './infrastructure/adapters/finance-dms.adapter';
import { RazorpayIfscAdapter } from './infrastructure/adapters/razorpay-ifsc.adapter';
import { IIfscLookupPort } from './application/ports/ifsc-lookup.port';
import { AccountBankIfscValidationService } from './application/services/account-bank-ifsc-validation.service';
import { DonationSummaryReportProvider } from './application/reports/donation-summary.provider';
import { AnnualAuditReportProvider } from './application/reports/annual-audit.provider';
import { FinanceReportReferenceDataService } from './application/reports/finance-report-reference-data.service';

import { CreateGuestDonorHandler } from './application/commands/create-guest-donor/create-guest-donor.handler';
import { UpdateGuestDonorHandler } from './application/commands/update-guest-donor/update-guest-donor.handler';
import { UpdateMemberDonorHandler } from './application/commands/update-member-donor/update-member-donor.handler';
import { MergeGuestDonorsHandler } from './application/commands/merge-guest-donors/merge-guest-donors.handler';
import { CreateDonationHandler } from './application/commands/create-donation/create-donation.handler';
import { CreateTransactionHandler } from './application/commands/create-transaction/create-transaction.handler';
import { SettleExpenseHandler } from './application/commands/settle-expense/settle-expense.handler';
import { CreateAccountHandler } from './application/commands/create-account/create-account.handler';
import { UpdateAccountHandler } from './application/commands/update-account/update-account.handler';
import { UpdateDonationHandler } from './application/commands/update-donation/update-donation.handler';
import { CreateExpenseHandler } from './application/commands/create-expense/create-expense.handler';
import { UpdateExpenseHandler } from './application/commands/update-expense/update-expense.handler';
import { FinalizeExpenseHandler } from './application/commands/finalize-expense/finalize-expense.handler';
import { CreateEarningHandler } from './application/commands/create-earning/create-earning.handler';
import { UpdateEarningHandler } from './application/commands/update-earning/update-earning.handler';
import { ReverseTransactionHandler } from './application/commands/reverse-transaction/reverse-transaction.handler';

import { ListDonorsHandler } from './application/queries/list-donors/list-donors.handler';
import { GetDonorByIdHandler } from './application/queries/get-donor-by-id/get-donor-by-id.handler';
import { GetDonorReferenceDataHandler } from './application/queries/get-donor-reference-data/get-donor-reference-data.handler';
import { ListDonationsHandler } from './application/queries/list-donations/list-donations.handler';
import { GetDonationByIdHandler } from './application/queries/get-donation-by-id/get-donation-by-id.handler';
import { GetDonationSummaryHandler } from './application/queries/get-donation-summary/get-donation-summary.handler';
import { GetDonationReferenceDataHandler } from './application/queries/get-donation-reference-data/get-donation-reference-data.handler';
import { ListAccountsHandler } from './application/queries/list-accounts/list-accounts.handler';
import { ListAccountTransactionsHandler } from './application/queries/list-account-transactions/list-account-transactions.handler';
import { GetPayableAccountsHandler } from './application/queries/get-payable-accounts/get-payable-accounts.handler';
import { GetAccountReferenceDataHandler } from './application/queries/get-account-reference-data/get-account-reference-data.handler';
import { GetIfscDetailsHandler } from './application/queries/get-ifsc-details/get-ifsc-details.handler';
import { ListExpensesHandler } from './application/queries/list-expenses/list-expenses.handler';
import { GetExpenseByIdHandler } from './application/queries/get-expense-by-id/get-expense-by-id.handler';
import { GetExpenseReferenceDataHandler } from './application/queries/get-expense-reference-data/get-expense-reference-data.handler';
import { ListEarningsHandler } from './application/queries/list-earnings/list-earnings.handler';
import { GetEarningByIdHandler } from './application/queries/get-earning-by-id/get-earning-by-id.handler';
import { GetEarningReferenceDataHandler } from './application/queries/get-earning-reference-data/get-earning-reference-data.handler';

import { OnUserCreatedFinanceHandler } from './application/handlers/events/on-user-created-finance/on-user-created-finance.handler';
import { OnUserDeletedFinanceHandler } from './application/handlers/events/on-user-deleted-finance/on-user-deleted-finance.handler';
import { DonationRaisedCorrespondenceResolver } from './application/notifications/donation-raised-correspondence.resolver';
import { DonationPaidCorrespondenceResolver } from './application/notifications/donation-paid-correspondence.resolver';

import { ReactivateDonorsHandler } from './application/handlers/queue/reactivate-donors.handler';
import { CreateDonationJobHandler } from './application/handlers/queue/create-donation.handler';
import { TriggerMonthlyDonationHandler } from './application/handlers/queue/trigger-monthly-donation.handler';
import { MarkDonationPendingHandler } from './application/handlers/queue/mark-donation-pending.handler';
import { RemindPendingDonationsHandler } from './application/handlers/queue/remind-pending-donations.handler';

import { LinkExpenseToActivityHandler } from './application/commands/link-expense-to-activity/link-expense-to-activity.handler';
import { AssertActivityCanCloseHandler } from './application/queries/assert-activity-can-close/assert-activity-can-close.handler';
import { FinanceFacade } from './application/services/finance.facade';

import { DonationController } from './presentation/controllers/donation.controller';
import { DonorController } from './presentation/controllers/donor.controller';
import { AccountController } from './presentation/controllers/account.controller';
import { ExpenseController } from './presentation/controllers/expense.controller';
import { EarningController } from './presentation/controllers/earning.controller';

const FinanceRequiredPortsGuard = createRequiredPortsGuard('FinanceModule', [
  {
    token: IFinanceReferenceDataPort,
    fixHint: 'Register { provide: IFinanceReferenceDataPort, useClass: FinanceReferenceDataAdapter } in IntegrationsModule.',
  },
]);

const COMMAND_HANDLERS = [
  CreateGuestDonorHandler,
  UpdateGuestDonorHandler,
  UpdateMemberDonorHandler,
  MergeGuestDonorsHandler,
  CreateDonationHandler,
  CreateTransactionHandler,
  SettleExpenseHandler,
  CreateAccountHandler,
  UpdateAccountHandler,
  UpdateDonationHandler,
  CreateExpenseHandler,
  UpdateExpenseHandler,
  FinalizeExpenseHandler,
  CreateEarningHandler,
  UpdateEarningHandler,
  ReverseTransactionHandler,
  LinkExpenseToActivityHandler,
];

const QUERY_HANDLERS = [
  ListDonorsHandler,
  GetDonorByIdHandler,
  GetDonorReferenceDataHandler,
  ListDonationsHandler,
  GetDonationByIdHandler,
  GetDonationSummaryHandler,
  GetDonationReferenceDataHandler,
  ListAccountsHandler,
  ListAccountTransactionsHandler,
  GetPayableAccountsHandler,
  GetAccountReferenceDataHandler,
  GetIfscDetailsHandler,
  ListExpensesHandler,
  GetExpenseByIdHandler,
  GetExpenseReferenceDataHandler,
  ListEarningsHandler,
  GetEarningByIdHandler,
  GetEarningReferenceDataHandler,
  AssertActivityCanCloseHandler,
];

const EVENT_HANDLERS = [OnUserCreatedFinanceHandler, OnUserDeletedFinanceHandler];

const NOTIFICATION_RESOLVERS = [
  DonationRaisedCorrespondenceResolver,
  DonationPaidCorrespondenceResolver,
];

const JOB_HANDLERS = [
  CreateDonationJobHandler,
  TriggerMonthlyDonationHandler,
  MarkDonationPendingHandler,
  RemindPendingDonationsHandler,
  ReactivateDonorsHandler,
];

export interface FinanceModuleAsyncOptions extends Pick<ModuleMetadata, 'imports'> {
  inject?: FactoryProvider['inject'];
  useFactory: (...args: any[]) => FinanceModuleInput | Promise<FinanceModuleInput>;
}

@Module({})
export class FinanceModule {
  static forRoot(options: FinanceModuleInput): DynamicModule {
    const parsed = FinanceModuleOptionsSchema.parse(options);
    return FinanceModule.buildModule([{ provide: FINANCE_OPTIONS, useValue: parsed }]);
  }

  static forRootAsync(asyncOptions: FinanceModuleAsyncOptions): DynamicModule {
    const optionsProvider: FactoryProvider = {
      provide: FINANCE_OPTIONS,
      inject: asyncOptions.inject ?? [],
      useFactory: async (...args: any[]) => FinanceModuleOptionsSchema.parse(await asyncOptions.useFactory(...args)),
    };
    return FinanceModule.buildModule([optionsProvider], asyncOptions.imports ?? []);
  }

  private static buildModule(optionProviders: Provider[], extraImports: any[] = []): DynamicModule {
    return {
      module: FinanceModule,
      imports: [CqrsModule, HttpModule.register({ timeout: 8000 }), ...extraImports],
      controllers: [DonationController, DonorController, AccountController, ExpenseController, EarningController],
      providers: [
        ...optionProviders,
        FinanceRequiredPortsGuard,
        { provide: IAccountRepository, useClass: AccountPrismaRepository },
        { provide: IDonationRepository, useClass: DonationPrismaRepository },
        { provide: IDonorRepository, useClass: DonorPrismaRepository },
        { provide: IExpenseRepository, useClass: ExpensePrismaRepository },
        { provide: IEarningRepository, useClass: EarningPrismaRepository },
        { provide: ITransactionRepository, useClass: TransactionPrismaRepository },
        FinanceDmsAdapter,
        AccountBankIfscValidationService,
        { provide: IIfscLookupPort, useClass: RazorpayIfscAdapter },
        FinanceFacade,
        FinanceReportReferenceDataService,
        DonationSummaryReportProvider,
        AnnualAuditReportProvider,
        ...COMMAND_HANDLERS,
        ...QUERY_HANDLERS,
        ...EVENT_HANDLERS,
        ...NOTIFICATION_RESOLVERS,
        ...JOB_HANDLERS,
      ],
      exports: [FinanceFacade, DonationSummaryReportProvider, AnnualAuditReportProvider],
    };
  }
}

