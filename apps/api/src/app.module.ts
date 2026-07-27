import 'dotenv/config';
import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { CoreModule } from '@nabarun-ngo/nestjs-shared-core';
import { CronModule } from '@nabarun-ngo/nestjs-shared-cron';
import { ReportingModule } from './modules/reporting/reporting.module';
import { LinksModule } from './modules/links/links.module';
import { IntegrationsModule } from './shared/integrations/integrations.module';
import { PUBLIC_SITE_MODULE } from './config/public-site-module.config';
import { DocumentGeneratorModule } from '@nabarun-ngo/nestjs-shared-document-generator';
import { join } from 'path';
import { AUTH_MODULE } from './config/auth-module.config';
import { QUEUE_MODULE } from './config/queue-module.config';
import { USER_MODULE } from './config/user-module.config';
import { FINANCE_MODULE } from './config/finance-module.config';
import { PROJECT_MODULE } from './config/project-module.config';
import { WORKFLOW_MODULE } from './config/workflow-module.config';
import { MEETING_MODULE } from './config/meeting-module.config';
import { PERSISTANCE_MODULE } from './config/database-module.config';
import { OBS_MODULE } from './config/obs-module.config';
import { DMS_MODULE } from './config/dms-module.config';
import { COMMENT_MODULE } from './config/comment-module.config';
import { CUSTOM_FORM_MODULE } from './config/custom-form-module.config';
import { CORRESPONDENCE_MODULE } from './config/correspondence-module.config';
import { HealthModule } from './modules/health/health.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    CoreModule,
    DocumentGeneratorModule.forRoot({
      watermarkPath: join(process.cwd(), 'src/assets/watermark.png'),
    }),
    CUSTOM_FORM_MODULE,
    PERSISTANCE_MODULE,
    HealthModule,
    AUTH_MODULE,
    OBS_MODULE,
    QUEUE_MODULE,
    IntegrationsModule.forRoot({ imports: [QUEUE_MODULE, WORKFLOW_MODULE] }),
    DMS_MODULE,
    COMMENT_MODULE,
    CronModule.forRoot({
      timezone: 'Asia/Kolkata',
    }),
    USER_MODULE,
    FINANCE_MODULE,
    WORKFLOW_MODULE,
    ReportingModule.forRoot({ imports: [WORKFLOW_MODULE, DMS_MODULE] }),
    PROJECT_MODULE,
    MEETING_MODULE,
    LinksModule.forRoot(),
    CORRESPONDENCE_MODULE,
    PUBLIC_SITE_MODULE,
  ],
})
export class AppModule { }
