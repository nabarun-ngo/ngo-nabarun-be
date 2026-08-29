import { DynamicModule, FactoryProvider, Module, ModuleMetadata, Provider } from '@nestjs/common';
import { CqrsModule } from '@nestjs/cqrs';
import { InvoiceModuleInput, InvoiceModuleOptionsSchema } from './invoice.schema';
import { INVOICE_OPTIONS } from './infrastructure/invoice-options.token';
import { IInvoiceRepository } from './domain/repositories/invoice.repository';
import { IInvoiceDocumentStore } from './domain/ports/invoice-document-store.port';
import { InvoicePrismaRepository } from '../../shared/persistence/invoice/repositories/invoice.prisma-repository';
import { InvoiceDmsAdapter } from './infrastructure/adapters/invoice-dms.adapter';
import { IssueInvoiceService } from './application/services/issue-invoice.service';
import { InvoiceFacade } from './application/services/invoice.facade';
import { IssueInvoiceHandler } from './application/commands/issue-invoice/issue-invoice.handler';
import { VoidIssuedInvoiceHandler } from './application/commands/void-issued-invoice/void-issued-invoice.handler';
import { GetIssuedInvoiceHandler } from './application/queries/get-issued-invoice/get-issued-invoice.handler';
import { ListIssuedInvoicesHandler } from './application/queries/list-issued-invoices/list-issued-invoices.handler';

const COMMAND_HANDLERS = [IssueInvoiceHandler, VoidIssuedInvoiceHandler];
const QUERY_HANDLERS = [GetIssuedInvoiceHandler, ListIssuedInvoicesHandler];

export interface InvoiceModuleAsyncOptions extends Pick<ModuleMetadata, 'imports'> {
  inject?: FactoryProvider['inject'];
  useFactory: (...args: any[]) => InvoiceModuleInput | Promise<InvoiceModuleInput>;
}

@Module({})
export class InvoiceModule {
  static forRoot(options: InvoiceModuleInput = {}): DynamicModule {
    const parsed = InvoiceModuleOptionsSchema.parse(options);
    return InvoiceModule.buildModule([{ provide: INVOICE_OPTIONS, useValue: parsed }]);
  }

  static forRootAsync(asyncOptions: InvoiceModuleAsyncOptions): DynamicModule {
    const optionsProvider: FactoryProvider = {
      provide: INVOICE_OPTIONS,
      inject: asyncOptions.inject ?? [],
      useFactory: async (...args: any[]) => InvoiceModuleOptionsSchema.parse(await asyncOptions.useFactory(...args)),
    };
    return InvoiceModule.buildModule([optionsProvider], asyncOptions.imports ?? []);
  }

  private static buildModule(optionProviders: Provider[], extraImports: any[] = []): DynamicModule {
    return {
      module: InvoiceModule,
      imports: [CqrsModule, ...extraImports],
      providers: [
        ...optionProviders,
        { provide: IInvoiceRepository, useClass: InvoicePrismaRepository },
        { provide: IInvoiceDocumentStore, useClass: InvoiceDmsAdapter },
        IssueInvoiceService,
        InvoiceFacade,
        ...COMMAND_HANDLERS,
        ...QUERY_HANDLERS,
      ],
      exports: [InvoiceFacade],
    };
  }
}
