import { DynamicModule, FactoryProvider, Module, ModuleMetadata, Provider } from '@nestjs/common';
import { CqrsModule } from '@nestjs/cqrs';
import { AssetModuleInput, AssetModuleOptions, AssetModuleOptionsSchema } from './asset.schema';
import { ASSET_OPTIONS } from './infrastructure/asset-options.token';
import { IAssetRepository } from './domain/repositories/asset.repository';
import { AssetPrismaRepository } from '../../shared/persistence/asset/repositories/asset.prisma-repository';

import { CreateAssetHandler } from './application/commands/create-asset/create-asset.handler';
import { UpdateAssetHandler } from './application/commands/update-asset/update-asset.handler';
import { DeleteAssetHandler } from './application/commands/delete-asset/delete-asset.handler';
import { AssignAssetCustodyHandler } from './application/commands/assign-asset-custody/assign-asset-custody.handler';
import { ReturnAssetCustodyHandler } from './application/commands/return-asset-custody/return-asset-custody.handler';

import { ListAssetsHandler } from './application/queries/list-assets/list-assets.handler';
import { GetAssetByIdHandler } from './application/queries/get-asset-by-id/get-asset-by-id.handler';

import { AssetController } from './presentation/controllers/asset.controller';

const COMMAND_HANDLERS = [
  CreateAssetHandler,
  UpdateAssetHandler,
  DeleteAssetHandler,
  AssignAssetCustodyHandler,
  ReturnAssetCustodyHandler,
];
const QUERY_HANDLERS = [ListAssetsHandler, GetAssetByIdHandler];

export interface AssetModuleAsyncOptions extends Pick<ModuleMetadata, 'imports'> {
  inject?: FactoryProvider['inject'];
  useFactory: (...args: any[]) => AssetModuleInput | Promise<AssetModuleInput>;
}

@Module({})
export class AssetModule {
  static forRoot(options: AssetModuleInput = {}): DynamicModule {
    const parsed = AssetModuleOptionsSchema.parse(options);
    return AssetModule.buildModule([{ provide: ASSET_OPTIONS, useValue: parsed }]);
  }

  static forRootAsync(asyncOptions: AssetModuleAsyncOptions): DynamicModule {
    const optionsProvider: FactoryProvider = {
      provide: ASSET_OPTIONS,
      inject: asyncOptions.inject ?? [],
      useFactory: async (...args: any[]) => AssetModuleOptionsSchema.parse(await asyncOptions.useFactory(...args)),
    };
    return AssetModule.buildModule([optionsProvider], asyncOptions.imports ?? []);
  }

  private static buildModule(optionProviders: Provider[], extraImports: any[] = []): DynamicModule {
    return {
      module: AssetModule,
      imports: [CqrsModule, ...extraImports],
      controllers: [AssetController],
      providers: [
        ...optionProviders,
        { provide: IAssetRepository, useClass: AssetPrismaRepository },
        ...COMMAND_HANDLERS,
        ...QUERY_HANDLERS,
      ],
      exports: [],
    };
  }
}

export type { AssetModuleOptions };
