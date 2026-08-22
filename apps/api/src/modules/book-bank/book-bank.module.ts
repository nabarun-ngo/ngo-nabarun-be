import { DynamicModule, FactoryProvider, Module, ModuleMetadata, Provider } from '@nestjs/common';
import { CqrsModule } from '@nestjs/cqrs';
import { BookBankModuleInput, BookBankModuleOptions, BookBankModuleOptionsSchema } from './book-bank.schema';
import { BOOK_BANK_OPTIONS } from './infrastructure/book-bank-options.token';
import { IBookRepository } from './domain/repositories/book.repository';
import { BookPrismaRepository } from '../../shared/persistence/book-bank/repositories/book.prisma-repository';

import { CreateBookHandler } from './application/commands/create-book/create-book.handler';
import { UpdateBookHandler } from './application/commands/update-book/update-book.handler';
import { DeleteBookHandler } from './application/commands/delete-book/delete-book.handler';
import { ApplyBookOperationHandler } from './application/commands/apply-book-operation/apply-book-operation.handler';

import { ListBooksHandler } from './application/queries/list-books/list-books.handler';
import { GetBookByIdHandler } from './application/queries/get-book-by-id/get-book-by-id.handler';
import { GetBookReferenceDataHandler } from './application/queries/get-book-reference-data/get-book-reference-data.handler';

import { BookController } from './presentation/controllers/book.controller';

const COMMAND_HANDLERS = [
  CreateBookHandler,
  UpdateBookHandler,
  DeleteBookHandler,
  ApplyBookOperationHandler,
];
const QUERY_HANDLERS = [ListBooksHandler, GetBookByIdHandler, GetBookReferenceDataHandler];

export interface BookBankModuleAsyncOptions extends Pick<ModuleMetadata, 'imports'> {
  inject?: FactoryProvider['inject'];
  useFactory: (...args: any[]) => BookBankModuleInput | Promise<BookBankModuleInput>;
}

@Module({})
export class BookBankModule {
  static forRoot(options: BookBankModuleInput = {}): DynamicModule {
    const parsed = BookBankModuleOptionsSchema.parse(options);
    return BookBankModule.buildModule([{ provide: BOOK_BANK_OPTIONS, useValue: parsed }]);
  }

  static forRootAsync(asyncOptions: BookBankModuleAsyncOptions): DynamicModule {
    const optionsProvider: FactoryProvider = {
      provide: BOOK_BANK_OPTIONS,
      inject: asyncOptions.inject ?? [],
      useFactory: async (...args: any[]) =>
        BookBankModuleOptionsSchema.parse(await asyncOptions.useFactory(...args)),
    };
    return BookBankModule.buildModule([optionsProvider], asyncOptions.imports ?? []);
  }

  private static buildModule(optionProviders: Provider[], extraImports: any[] = []): DynamicModule {
    return {
      module: BookBankModule,
      imports: [CqrsModule, ...extraImports],
      controllers: [BookController],
      providers: [
        ...optionProviders,
        { provide: IBookRepository, useClass: BookPrismaRepository },
        ...COMMAND_HANDLERS,
        ...QUERY_HANDLERS,
      ],
      exports: [],
    };
  }
}

export type { BookBankModuleOptions };
