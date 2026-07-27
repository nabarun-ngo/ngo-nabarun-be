import { DynamicModule, FactoryProvider, ModuleMetadata, Module, Provider } from '@nestjs/common';
import { CqrsModule } from '@nestjs/cqrs';
import { createRequiredPortsGuard } from '@nabarun-ngo/nestjs-shared-core';

import { UserModuleInput, UserModuleOptions, UserModuleOptionsSchema } from './user.schema';
import { USER_OPTIONS } from './infrastructure/user-options.token';

import { IUserRepository } from './domain/repositories/user.repository';
import { IIdentityProvider } from './domain/ports/identity-provider.port';
import { IUserReferenceDataPort } from './application/ports/user-reference-data.port';

import { IUserLookupPort } from '@nabarun-ngo/nestjs-shared-core';

import { UserPrismaRepository } from '../../shared/persistence/user/user.prisma-repository';
import { Auth0IdentityAdapter } from './infrastructure/external/auth0-identity.adapter';
import { UserLookupAdapter } from './infrastructure/adapters/user-lookup.adapter';
import { UserDonationScheduleReadAdapter } from './infrastructure/adapters/user-donation-schedule-read.adapter';
import { IUserDonationScheduleReadPort } from './domain/ports/user-donation-schedule-read.port';
import { UserFacade } from './application/services/user.facade';

import { CreateUserHandler } from './application/commands/create-user/create-user.handler';
import { UpdateUserProfileHandler } from './application/commands/update-user-profile/update-user-profile.handler';
import { UpdateUserAdminHandler } from './application/commands/update-user-admin/update-user-admin.handler';
import { InitiatePasswordChangeHandler } from './application/commands/initiate-password-change/initiate-password-change.handler';
import { DeleteUserHandler } from './application/commands/delete-user/delete-user.handler';
import { GrantUserConnectionHandler } from './application/commands/grant-user-connection/grant-user-connection.handler';
import { RevokeUserConnectionHandler } from './application/commands/revoke-user-connection/revoke-user-connection.handler';

import { GetUserByIdHandler } from './application/queries/get-user-by-id/get-user-by-id.handler';
import { GetMyProfileHandler } from './application/queries/get-my-profile/get-my-profile.handler';
import { ListUsersHandler } from './application/queries/list-users/list-users.handler';
import { GetUserReferenceDataHandler } from './application/queries/get-user-reference-data/get-user-reference-data.handler';
import { GetUserConnectionsHandler } from './application/queries/get-user-connections/get-user-connections.handler';
import { GetUserByEmailHandler } from './application/queries/get-user-by-email/get-user-by-email.handler';

import { OnUserCreatedHandler } from './application/handlers/events/on-user-created/on-user-created.handler';
import { OnUserProfileUpdatedHandler } from './application/handlers/events/on-user-profile-updated/on-user-profile-updated.handler';
import { OnUserDeletedHandler } from './application/handlers/events/on-user-deleted/on-user-deleted.handler';
import { OnUserStatusChangedHandler } from './application/handlers/events/on-user-status-changed/on-user-status-changed.handler';

import { UserCreatedCorrespondenceResolver } from './application/notifications/user-created-correspondence.resolver';
import { UserDeletedCorrespondenceResolver } from './application/notifications/user-deleted-correspondence.resolver';

import { Auth0UserCreationHandler } from './application/handlers/workflow/auth0-user-creation.handler';
import { UserNotRegisteredTaskHandler } from './application/handlers/workflow/user-not-registered.handler';
import { UserDeleteAndDataCleanupHandler } from './application/handlers/workflow/user-delete-cleanup.handler';

import { UserController } from './presentation/controllers/user.controller';

const UserRequiredPortsGuard = createRequiredPortsGuard('UserModule', [
  {
    token: IUserReferenceDataPort,
    fixHint:
      'Register { provide: IUserReferenceDataPort, useClass: UserReferenceDataAdapter } in IntegrationsModule.',
  },
]);

const COMMAND_HANDLERS = [
  CreateUserHandler,
  UpdateUserProfileHandler,
  UpdateUserAdminHandler,
  InitiatePasswordChangeHandler,
  DeleteUserHandler,
  GrantUserConnectionHandler,
  RevokeUserConnectionHandler,
];

const QUERY_HANDLERS = [
  GetUserByIdHandler,
  GetMyProfileHandler,
  ListUsersHandler,
  GetUserReferenceDataHandler,
  GetUserConnectionsHandler,
  GetUserByEmailHandler,
];

const EVENT_HANDLERS = [
  OnUserCreatedHandler,
  OnUserProfileUpdatedHandler,
  OnUserDeletedHandler,
  OnUserStatusChangedHandler,
];

// Pure correspondence resolvers for user-domain events. Discovered app-wide by
// the correspondence package's DiscoveryService — registered here to co-locate
// them with the events they react to.
const NOTIFICATION_RESOLVERS = [
  UserCreatedCorrespondenceResolver,
  UserDeletedCorrespondenceResolver,
];

const WORKFLOW_HANDLERS = [
  Auth0UserCreationHandler,
  UserNotRegisteredTaskHandler,
  UserDeleteAndDataCleanupHandler,
];

export interface UserModuleAsyncOptions extends Pick<ModuleMetadata, 'imports'> {
  inject?: FactoryProvider['inject'];
  useFactory: (...args: any[]) => UserModuleInput | Promise<UserModuleInput>;
}

@Module({})
export class UserModule {
  static forRoot(options: UserModuleInput): DynamicModule {
    const parsed = UserModuleOptionsSchema.parse(options);
    return UserModule.buildModule([{ provide: USER_OPTIONS, useValue: parsed }]);
  }

  static forRootAsync(asyncOptions: UserModuleAsyncOptions): DynamicModule {
    const optionsProvider: FactoryProvider = {
      provide: USER_OPTIONS,
      inject: asyncOptions.inject ?? [],
      useFactory: async (...args: any[]) => {
        const raw = await asyncOptions.useFactory(...args);
        return UserModuleOptionsSchema.parse(raw);
      },
    };
    return UserModule.buildModule([optionsProvider], asyncOptions.imports ?? []);
  }

  private static buildModule(
    optionProviders: Provider[],
    extraImports: any[] = [],
  ): DynamicModule {
    return {
      module: UserModule,
      imports: [CqrsModule, ...extraImports],
      controllers: [UserController],
      providers: [
        ...optionProviders,
        UserRequiredPortsGuard,

        { provide: IUserRepository, useClass: UserPrismaRepository },
        { provide: IIdentityProvider, useClass: Auth0IdentityAdapter },

        UserLookupAdapter,
        UserDonationScheduleReadAdapter,
        UserFacade,
        { provide: IUserLookupPort, useClass: UserLookupAdapter },
        { provide: IUserDonationScheduleReadPort, useClass: UserDonationScheduleReadAdapter },

        ...COMMAND_HANDLERS,
        ...QUERY_HANDLERS,
        ...EVENT_HANDLERS,
        ...NOTIFICATION_RESOLVERS,
        ...WORKFLOW_HANDLERS,
      ],
      exports: [
        IUserLookupPort,
        IUserDonationScheduleReadPort,
        UserFacade,
      ],
    };
  }
}
