import { DynamicModule, Module } from '@nestjs/common';
import { CqrsModule } from '@nestjs/cqrs';
import { ApproveRequestHandler } from './application/commands/approve-request/approve-request.handler';
import { AssignRequestHandler } from './application/commands/assign-request/assign-request.handler';
import { CloseRequestHandler } from './application/commands/close-request/close-request.handler';
import { CreateRequestHandler } from './application/commands/create-request/create-request.handler';
import { RejectRequestHandler } from './application/commands/reject-request/reject-request.handler';
import { StartRequestHandler } from './application/commands/start-request/start-request.handler';
import { WithdrawRequestHandler } from './application/commands/withdraw-request/withdraw-request.handler';
import { GetRequestStartFormHandler } from './application/queries/get-request-start-form/get-request-start-form.handler';
import { GetRequestHandler } from './application/queries/get-request/get-request.handler';
import { ListRequestTypesHandler } from './application/queries/list-request-types/list-request-types.handler';
import { ListRequestsHandler } from './application/queries/list-requests/list-requests.handler';
import { IRequestDefinitionPort } from './domain/ports/request-definition.port';
import { IRequestRepository } from './domain/repositories/request.repository';
import { RequestDefinitionAdapter } from './infrastructure/adapters/request-definition.adapter';
import { RequestController } from './presentation/controllers/request.controller';
import { RequestPrismaRepository } from '../../shared/persistence/request/repositories/request.prisma-repository';

const QUERY_HANDLERS = [
  ListRequestTypesHandler,
  GetRequestStartFormHandler,
  ListRequestsHandler,
  GetRequestHandler,
];

const COMMAND_HANDLERS = [
  CreateRequestHandler,
  StartRequestHandler,
  AssignRequestHandler,
  CloseRequestHandler,
  ApproveRequestHandler,
  RejectRequestHandler,
  WithdrawRequestHandler,
];

@Module({})
export class RequestModule {
  static forRoot(): DynamicModule {
    return {
      module: RequestModule,
      global: true,
      imports: [CqrsModule],
      controllers: [RequestController],
      providers: [
        { provide: IRequestDefinitionPort, useClass: RequestDefinitionAdapter },
        { provide: IRequestRepository, useClass: RequestPrismaRepository },
        ...QUERY_HANDLERS,
        ...COMMAND_HANDLERS,
      ],
      exports: [IRequestRepository],
    };
  }
}
