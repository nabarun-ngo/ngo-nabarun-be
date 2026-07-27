import { DynamicModule, Global, Module, ModuleMetadata } from '@nestjs/common';
import { ILockingPort } from '@nabarun-ngo/nestjs-shared-persistence';
import {
  IApiKeyRepository,
  IPermissionRepository,
  IRoleGroupRepository,
  IRoleRepository,
  IUserRoleGroupRepository,
  IUserRoleRepository,
} from '@nabarun-ngo/nestjs-shared-auth';
import { ICommentRepository } from '@nabarun-ngo/nestjs-shared-comment';
import {
  IFormRepository,
  IFormSubmissionRepository,
} from '@nabarun-ngo/nestjs-shared-custom-forms';
import {
  INotificationRepository,
  IResourceSubscriptionRepository,
  IUserNotificationRepository,
} from '@nabarun-ngo/nestjs-shared-correspondence';
import { IDocumentRepository } from '@nabarun-ngo/nestjs-shared-dms';
import { IJsonDocumentRepository } from '@nabarun-ngo/nestjs-shared-json-store';
import {
  IOAuthAccountRepository,
  IOAuthTokenRepository,
} from '@nabarun-ngo/nestjs-shared-token-vault';
import {
  IWorkflowEventLogRepository,
  IWorkflowIdempotencyRepository,
  IWorkflowInboxRepository,
  IWorkflowInstanceRepository,
  IWorkflowOutboxRepository,
  IWorkflowTokenRepository,
} from '@nabarun-ngo/nestjs-shared-workflow';
import { ApiKeyPrismaRepository } from './auth/repositories/api-key.prisma-repository';
import { PermissionPrismaRepository } from './auth/repositories/permission.prisma-repository';
import { RoleGroupPrismaRepository } from './auth/repositories/role-group.prisma-repository';
import { RolePrismaRepository } from './auth/repositories/role.prisma-repository';
import { UserRoleGroupPrismaRepository } from './auth/repositories/user-role-group.prisma-repository';
import { UserRolePrismaRepository } from './auth/repositories/user-role.prisma-repository';
import { PrismaCommentRepository } from './comment/repositories/comment.prisma-repository';
import { NotificationPrismaRepository } from './correspondence/repositories/notification.prisma-repository';
import { ResourceSubscriptionPrismaRepository } from './correspondence/repositories/resource-subscription.prisma-repository';
import { UserNotificationPrismaRepository } from './correspondence/repositories/user-notification.prisma-repository';
import { FormPrismaRepository } from './custom-forms/repositories/form.prisma-repository';
import { FormSubmissionPrismaRepository } from './custom-forms/repositories/form-submission.prisma-repository';
import { DocumentPrismaRepository } from './dms/repositories/document.prisma-repository';
import { JsonDocumentPrismaRepository } from './json-store/repositories/json-document.prisma-repository';
import { OAuthAccountPrismaRepository } from './token-vault/repositories/oauth-account.prisma-repository';
import { OAuthTokenPrismaRepository } from './token-vault/repositories/oauth-token.prisma-repository';
import { WorkflowEventLogPrismaRepository } from './workflow/repositories/workflow-event-log.prisma-repository';
import { WorkflowIdempotencyPrismaRepository } from './workflow/repositories/workflow-idempotency.prisma-repository';
import { WorkflowInboxPrismaRepository } from './workflow/repositories/workflow-inbox.prisma-repository';
import { WorkflowInstancePrismaRepository } from './workflow/repositories/workflow-instance.prisma-repository';
import { WorkflowOutboxPrismaRepository } from './workflow/repositories/workflow-outbox.prisma-repository';
import { WorkflowTokenPrismaRepository } from './workflow/repositories/workflow-token.prisma-repository';
import { PostgresAdvisoryLockingAdapter } from './locking/adapter/postgres-advisory-locking.adapter';

export interface PersistenceModuleOptions {
  /** Pass the same QueueModule.forRoot/forRootAsync dynamic module used by the app. */
  imports?: ModuleMetadata['imports'];
}

const LOCKING_PROVIDER = {
  provide: ILockingPort,
  useClass: PostgresAdvisoryLockingAdapter,
} as const;

const REPOSITORY_PROVIDERS = [
  { provide: IOAuthAccountRepository, useClass: OAuthAccountPrismaRepository },
  { provide: IOAuthTokenRepository, useClass: OAuthTokenPrismaRepository },
  { provide: ICommentRepository, useClass: PrismaCommentRepository },
  { provide: IFormRepository, useClass: FormPrismaRepository },
  { provide: IFormSubmissionRepository, useClass: FormSubmissionPrismaRepository },
  { provide: IDocumentRepository, useClass: DocumentPrismaRepository },
  { provide: IJsonDocumentRepository, useClass: JsonDocumentPrismaRepository },
  { provide: INotificationRepository, useClass: NotificationPrismaRepository },
  { provide: IUserNotificationRepository, useClass: UserNotificationPrismaRepository },
  { provide: IResourceSubscriptionRepository, useClass: ResourceSubscriptionPrismaRepository },
  { provide: IRoleRepository, useClass: RolePrismaRepository },
  { provide: IRoleGroupRepository, useClass: RoleGroupPrismaRepository },
  { provide: IPermissionRepository, useClass: PermissionPrismaRepository },
  { provide: IUserRoleRepository, useClass: UserRolePrismaRepository },
  { provide: IUserRoleGroupRepository, useClass: UserRoleGroupPrismaRepository },
  { provide: IApiKeyRepository, useClass: ApiKeyPrismaRepository },
  { provide: IWorkflowInstanceRepository, useClass: WorkflowInstancePrismaRepository },
  { provide: IWorkflowEventLogRepository, useClass: WorkflowEventLogPrismaRepository },
  { provide: IWorkflowInboxRepository, useClass: WorkflowInboxPrismaRepository },
  { provide: IWorkflowTokenRepository, useClass: WorkflowTokenPrismaRepository },
  { provide: IWorkflowOutboxRepository, useClass: WorkflowOutboxPrismaRepository },
  { provide: IWorkflowIdempotencyRepository, useClass: WorkflowIdempotencyPrismaRepository },
] as const;


@Global()
@Module({})
export class PersistenceModule {
  static forRoot(options: PersistenceModuleOptions = {}): DynamicModule {
    return {
      module: PersistenceModule,
      imports: [...(options.imports ?? [])],
      providers: [LOCKING_PROVIDER, ...REPOSITORY_PROVIDERS],
      exports: [ILockingPort, ...REPOSITORY_PROVIDERS.map(({ provide }) => provide)],
    };
  }
}
