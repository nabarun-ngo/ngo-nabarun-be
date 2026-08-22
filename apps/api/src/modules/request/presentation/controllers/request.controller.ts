import {
  Body,
  Controller,
  Get,
  Param,
  Post,
  Query,
  UseGuards,
} from '@nestjs/common';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import {
  ApiBearerAuth,
  ApiSecurity,
  ApiTags,
} from '@nestjs/swagger';
import {
  CurrentUser,
  RequirePermissions,
  UnifiedAuthGuard,
  type AuthUser,
} from '@nabarun-ngo/nestjs-shared-auth';
import {
  ApiAutoPagedResponse,
  ApiAutoResponse,
  ApiPaginationQuery,
  ApiUuidParam,
  PagedResponse,
} from '@nabarun-ngo/nestjs-shared-core';
import { ApproveRequestCommand } from '../../application/commands/approve-request/approve-request.command';
import { AssignRequestCommand } from '../../application/commands/assign-request/assign-request.command';
import { CloseRequestCommand } from '../../application/commands/close-request/close-request.command';
import { CreateRequestCommand } from '../../application/commands/create-request/create-request.command';
import { RejectRequestCommand } from '../../application/commands/reject-request/reject-request.command';
import { StartRequestCommand } from '../../application/commands/start-request/start-request.command';
import { WithdrawRequestCommand } from '../../application/commands/withdraw-request/withdraw-request.command';
import {
  AssignRequestDto,
  CreateRequestDto,
  DecisionNoteDto,
  ListRequestsQueryDto,
  RequestDto,
  RequestStartFormDto,
  RequestTypeDto,
  WithdrawRequestDto,
} from '../../application/dtos/request.dto';
import { GetRequestStartFormQuery } from '../../application/queries/get-request-start-form/get-request-start-form.query';
import { GetRequestQuery } from '../../application/queries/get-request/get-request.query';
import { ListRequestTypesQuery } from '../../application/queries/list-request-types/list-request-types.query';
import { ListRequestsQuery } from '../../application/queries/list-requests/list-requests.query';

@ApiTags('Requests')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('requests')
export class RequestController {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) {}

  @Get('types')
  @RequirePermissions('read:requests', 'create:requests')
  @ApiAutoResponse(RequestTypeDto, { isArray: true })
  listTypes(): Promise<RequestTypeDto[]> {
    return this.queryBus.execute(new ListRequestTypesQuery());
  }

  @Get('types/:type/start-form')
  @RequirePermissions('create:requests', 'read:requests')
  @ApiAutoResponse(RequestStartFormDto)
  getStartForm(@Param('type') type: string): Promise<RequestStartFormDto> {
    return this.queryBus.execute(new GetRequestStartFormQuery(type));
  }

  @Post()
  @RequirePermissions('create:requests')
  @ApiAutoResponse(RequestDto, { status: 201 })
  create(
    @Body() dto: CreateRequestDto,
    @CurrentUser() user: AuthUser,
  ): Promise<RequestDto> {
    return this.commandBus.execute(new CreateRequestCommand(dto, user));
  }

  @Get()
  @RequirePermissions('read:requests')
  @ApiPaginationQuery()
  @ApiAutoPagedResponse(RequestDto)
  list(
    @Query() query: ListRequestsQueryDto,
    @CurrentUser() user: AuthUser,
  ): Promise<PagedResponse<RequestDto>> {
    return this.queryBus.execute(new ListRequestsQuery(query, user));
  }

  @Get(':id')
  @RequirePermissions('read:requests')
  @ApiUuidParam('id')
  @ApiAutoResponse(RequestDto)
  getById(
    @Param('id') id: string,
    @CurrentUser() user: AuthUser,
  ): Promise<RequestDto> {
    return this.queryBus.execute(new GetRequestQuery(id, user));
  }

  @Post(':id/start')
  @RequirePermissions('update:requests')
  @ApiUuidParam('id')
  @ApiAutoResponse(RequestDto)
  start(
    @Param('id') id: string,
    @CurrentUser() user: AuthUser,
  ): Promise<RequestDto> {
    return this.commandBus.execute(new StartRequestCommand(id, user));
  }

  @Post(':id/assign')
  @RequirePermissions('update:requests')
  @ApiUuidParam('id')
  @ApiAutoResponse(RequestDto)
  assign(
    @Param('id') id: string,
    @Body() dto: AssignRequestDto,
    @CurrentUser() user: AuthUser,
  ): Promise<RequestDto> {
    return this.commandBus.execute(new AssignRequestCommand(id, dto.assigneeId, user));
  }

  @Post(':id/close')
  @RequirePermissions('update:requests')
  @ApiUuidParam('id')
  @ApiAutoResponse(RequestDto)
  close(
    @Param('id') id: string,
    @Body() dto: DecisionNoteDto,
    @CurrentUser() user: AuthUser,
  ): Promise<RequestDto> {
    return this.commandBus.execute(new CloseRequestCommand(id, dto?.note, user));
  }

  @Post(':id/approve')
  @RequirePermissions('update:requests')
  @ApiUuidParam('id')
  @ApiAutoResponse(RequestDto)
  approve(
    @Param('id') id: string,
    @Body() dto: DecisionNoteDto,
    @CurrentUser() user: AuthUser,
  ): Promise<RequestDto> {
    return this.commandBus.execute(new ApproveRequestCommand(id, dto?.note, user));
  }

  @Post(':id/reject')
  @RequirePermissions('update:requests')
  @ApiUuidParam('id')
  @ApiAutoResponse(RequestDto)
  reject(
    @Param('id') id: string,
    @Body() dto: DecisionNoteDto,
    @CurrentUser() user: AuthUser,
  ): Promise<RequestDto> {
    return this.commandBus.execute(new RejectRequestCommand(id, dto?.note, user));
  }

  @Post(':id/withdraw')
  @RequirePermissions('update:requests', 'create:requests')
  @ApiUuidParam('id')
  @ApiAutoResponse(RequestDto)
  withdraw(
    @Param('id') id: string,
    @Body() dto: WithdrawRequestDto,
    @CurrentUser() user: AuthUser,
  ): Promise<RequestDto> {
    return this.commandBus.execute(new WithdrawRequestCommand(id, dto?.note, user));
  }
}
