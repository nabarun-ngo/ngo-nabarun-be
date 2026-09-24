import { DynamicModule, Module, ModuleMetadata } from '@nestjs/common';
import { CqrsModule } from '@nestjs/cqrs';
import { GetStaticContentHandler } from './application/queries/get-static-content/get-static-content.handler';
import { GetDynamicContentHandler } from './application/queries/get-dynamic-content/get-dynamic-content.handler';
import { SubmitDynamicPublicFormHandler } from './application/commands/submit-dynamic-public-form/submit-dynamic-public-form.handler';
import { SubscribeNewsletterHandler } from './application/commands/subscribe-newsletter/subscribe-newsletter.handler';
import { GetDynamicPublicFormDefinitionHandler } from './application/queries/get-dynamic-public-form-definition/get-dynamic-public-form-definition.handler';
import { NoOpNewsletterSubscriptionAdapter } from './infrastructure/adapters/noop-newsletter-subscription.adapter';
import { NEWSLETTER_SUBSCRIPTION_PORT } from './domain/ports/newsletter-subscription.port';
import { PublicSiteContentsController } from './presentation/controllers/public-site-contents.controller';
import { PublicSiteFormsController } from './presentation/controllers/public-site-forms.controller';
import { NewsletterController } from './presentation/controllers/newsletter.controller';
import {
  PUBLIC_SITE_DEFAULT_SUBMITTED_BY_ID,
  PUBLIC_SITE_OPTIONS,
  PublicSiteOptions,
} from './public-site.options';

const QUERY_HANDLERS = [
  GetStaticContentHandler,
  GetDynamicContentHandler,
  GetDynamicPublicFormDefinitionHandler,
];

const COMMAND_HANDLERS = [
  SubmitDynamicPublicFormHandler,
  SubscribeNewsletterHandler,
];

@Module({})
export class PublicSiteModule {
  static forRoot(
    options: PublicSiteOptions & { imports?: ModuleMetadata['imports'] } = {},
  ): DynamicModule {
    const { imports, submittedById } = options;
    const runtimeOptions: PublicSiteOptions = {
      submittedById: submittedById ?? PUBLIC_SITE_DEFAULT_SUBMITTED_BY_ID,
    };

    return {
      module: PublicSiteModule,
      imports: [CqrsModule, ...(imports ?? [])],
      controllers: [
        PublicSiteContentsController,
        PublicSiteFormsController,
        NewsletterController,
      ],
      providers: [
        { provide: PUBLIC_SITE_OPTIONS, useValue: runtimeOptions },
        {
          provide: NEWSLETTER_SUBSCRIPTION_PORT,
          useClass: NoOpNewsletterSubscriptionAdapter,
        },
        ...QUERY_HANDLERS,
        ...COMMAND_HANDLERS,
      ],
    };
  }
}
