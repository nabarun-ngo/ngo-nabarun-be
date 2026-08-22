import { DynamicModule, Module } from '@nestjs/common';
import { CqrsModule } from '@nestjs/cqrs';
import { IHelpPortalContentPort } from './domain/ports/help-portal-content.port';
import { HelpPortalContentAdapter } from './infrastructure/adapters/help-portal-content.adapter';
import { GetHelpPortalCatalogHandler } from './application/queries/get-help-portal-catalog/get-help-portal-catalog.handler';
import { GetHelpPortalArticleHandler } from './application/queries/get-help-portal-article/get-help-portal-article.handler';
import { HelpPortalController } from './presentation/controllers/help-portal.controller';

const QUERY_HANDLERS = [GetHelpPortalCatalogHandler, GetHelpPortalArticleHandler];

@Module({})
export class HelpPortalModule {
  static forRoot(): DynamicModule {
    return {
      module: HelpPortalModule,
      imports: [CqrsModule],
      controllers: [HelpPortalController],
      providers: [
        { provide: IHelpPortalContentPort, useClass: HelpPortalContentAdapter },
        ...QUERY_HANDLERS,
      ],
    };
  }
}
