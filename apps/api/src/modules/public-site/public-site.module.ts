import { DynamicModule, Module, ModuleMetadata } from '@nestjs/common';
import { CqrsModule } from '@nestjs/cqrs';
import { GetStaticContentHandler } from './application/queries/get-static-content/get-static-content.handler';
import { GetDynamicContentHandler } from './application/queries/get-dynamic-content/get-dynamic-content.handler';
import { SubmitPublicWorkflowFormHandler } from './application/commands/submit-public-workflow-form/submit-public-workflow-form.handler';
import { SubmitDynamicPublicFormHandler } from './application/commands/submit-dynamic-public-form/submit-dynamic-public-form.handler';
import { SubscribeNewsletterHandler } from './application/commands/subscribe-newsletter/subscribe-newsletter.handler';
import { GetPublicWorkflowFormDefinitionHandler } from './application/queries/get-public-workflow-form-definition/get-public-workflow-form-definition.handler';
import { GetDynamicPublicFormDefinitionHandler } from './application/queries/get-dynamic-public-form-definition/get-dynamic-public-form-definition.handler';
import { PUBLIC_SITE_STATIC_CONTENT_PORT } from './domain/ports/public-site-static-content.port';
import { PUBLIC_SITE_DYNAMIC_CONTENT_PORT } from './domain/ports/public-site-dynamic-content.port';
import { PublicSiteStaticContentAdapter } from './infrastructure/adapters/public-site-static-content.adapter';
import { PublicSiteDynamicContentAdapter } from './infrastructure/adapters/public-site-dynamic-content.adapter';
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
  GetPublicWorkflowFormDefinitionHandler,
  GetDynamicPublicFormDefinitionHandler,
];

const COMMAND_HANDLERS = [
  SubmitPublicWorkflowFormHandler,
  SubmitDynamicPublicFormHandler,
  SubscribeNewsletterHandler,
];

@Module({})
export class PublicSiteModule {
  static forRoot(
    options: PublicSiteOptions & { imports?: ModuleMetadata['imports'] },
  ): DynamicModule {
    const {
      imports,
      submittedById,
      publicWorkflows,
      staticContent:{
        namespace,
        key,
      },
    } = options;
    const runtimeOptions: PublicSiteOptions = {
      publicWorkflows,
      staticContent:{
        namespace,
        key,
      },
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
          provide: PUBLIC_SITE_STATIC_CONTENT_PORT,
          useClass: PublicSiteStaticContentAdapter,
        },
        {
          provide: PUBLIC_SITE_DYNAMIC_CONTENT_PORT,
          useClass: PublicSiteDynamicContentAdapter,
        },
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
