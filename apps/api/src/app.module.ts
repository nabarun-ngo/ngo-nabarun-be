import 'dotenv/config';
import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { CoreModule } from '@nabarun-ngo/nestjs-shared-core';
import { CronModule } from '@nabarun-ngo/nestjs-shared-cron';
import { ReportingModule } from './modules/reporting/reporting.module';
import { HelpPortalModule } from './modules/help-portal/help-portal.module';
import { RequestModule } from './modules/request/request.module';
import { IntegrationsModule } from './shared/integrations/integrations.module';
import { PUBLIC_SITE_MODULE } from './config/public-site-module.config';
import { DocumentGeneratorModule } from '@nabarun-ngo/nestjs-shared-document-generator';
import { join } from 'path';
import { AUTH_MODULE } from './config/auth-module.config';
import { QUEUE_MODULE } from './config/queue-module.config';
import { USER_MODULE } from './config/user-module.config';
import { FINANCE_MODULE } from './config/finance-module.config';
import { PROJECT_MODULE } from './config/project-module.config';
import { MEETING_MODULE } from './config/meeting-module.config';
import { ASSET_MODULE } from './config/asset-module.config';
import { BOOK_BANK_MODULE } from './config/book-bank-module.config';
import { PERSISTANCE_MODULE } from './config/database-module.config';
import { OBS_MODULE } from './config/obs-module.config';
import { DMS_MODULE } from './config/dms-module.config';
import { COMMENT_MODULE } from './config/comment-module.config';
import { CUSTOM_FORM_MODULE } from './config/custom-form-module.config';
import { CORRESPONDENCE_MODULE } from './config/correspondence-module.config';
import { HEALTH_MODULE } from './config/health-module.config';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    CoreModule,
    DocumentGeneratorModule.forRoot({
      watermarkPath: join(process.cwd(), 'src/assets/watermark.png'),
    }),
    CUSTOM_FORM_MODULE,
    PERSISTANCE_MODULE,
    HEALTH_MODULE,
    AUTH_MODULE,
    OBS_MODULE,
    QUEUE_MODULE,
    RequestModule.forRoot(),
    IntegrationsModule.forRoot({ imports: [QUEUE_MODULE, USER_MODULE, PROJECT_MODULE] }),
    DMS_MODULE,
    COMMENT_MODULE,
    CronModule.forRoot({
      timezone: 'Asia/Kolkata',
    }),
    USER_MODULE,
    FINANCE_MODULE,
    ReportingModule.forRoot({ imports: [DMS_MODULE] }),
    PROJECT_MODULE,
    MEETING_MODULE,
    ASSET_MODULE,
    BOOK_BANK_MODULE,
    HelpPortalModule.forRoot(),
    CORRESPONDENCE_MODULE,
    PUBLIC_SITE_MODULE,
  ],
})
export class AppModule { }
