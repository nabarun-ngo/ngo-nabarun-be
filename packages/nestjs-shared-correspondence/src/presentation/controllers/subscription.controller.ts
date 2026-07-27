import { Body, Controller, Delete, Get, Param, Patch, Post, Query, UseGuards } from '@nestjs/common';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { ApiAutoResponse, ApiAutoVoidResponse } from '@nabarun-ngo/nestjs-shared-core';
import {
  UnifiedAuthGuard,
  PermissionsGuard,
  RequirePermissions,
  CurrentUser,
  AuthUser,
  requireUserId,
} from '@nabarun-ngo/nestjs-shared-auth';
import { SubscribeUserCommand } from '../../application/commands/subscribe-user/subscribe-user.command';
import { UnsubscribeUserCommand } from '../../application/commands/unsubscribe-user/unsubscribe-user.command';
import { UpdateChannelConfigCommand } from '../../application/commands/update-channel-config/update-channel-config.command';
import { GetUserSubscriptionsQuery } from '../../application/queries/get-user-subscriptions/get-user-subscriptions.query';
import { GetResourceSubscribersQuery } from '../../application/queries/get-resource-subscribers/get-resource-subscribers.query';
import { SubscribeUserRequestDto, UpdateChannelConfigRequestDto } from '../../application/dtos/subscription.request.dto';
import { SubscriptionResponseDto } from '../../application/dtos/subscription-response.dto';

@ApiTags('correspondence / subscriptions')
@Controller('correspondence/subscriptions')
@UseGuards(UnifiedAuthGuard, PermissionsGuard)
export class SubscriptionController {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) { }

  @Get('me')
  @RequirePermissions('read:subscriptions')
  @ApiOperation({ summary: 'List subscriptions for the current user' })
  @ApiAutoResponse(SubscriptionResponseDto, { isArray: true })
  async listMine(
    @CurrentUser() user: AuthUser,
    @Query('resourceType') resourceType?: string,
    @Query('resourceId') resourceId?: string,
  ): Promise<SubscriptionResponseDto[]> {
    return this.queryBus.execute(new GetUserSubscriptionsQuery(requireUserId(user), resourceType, resourceId));
  }

  @Post()
  @RequirePermissions('create:subscriptions')
  @ApiOperation({ summary: 'Subscribe current user to a resource' })
  @ApiAutoVoidResponse({ status: 201 })
  async subscribe(@Body() body: SubscribeUserRequestDto, @CurrentUser() user: AuthUser): Promise<void> {
    return this.commandBus.execute(
      new SubscribeUserCommand(
        requireUserId(user),
        user.email || undefined,
        body.resourceType,
        body.via,
        undefined,
        body.resourceId,
        body.channels,
      ),
    );
  }

  @Delete(':id')
  @RequirePermissions('delete:subscriptions')
  @ApiOperation({ summary: 'Unsubscribe current user from a resource' })
  @ApiAutoVoidResponse()
  async unsubscribe(@Param('id') subscriptionId: string, @CurrentUser() user: AuthUser): Promise<void> {
    return this.commandBus.execute(
      new UnsubscribeUserCommand(requireUserId(user), undefined, undefined, subscriptionId),
    );
  }

  @Patch(':id/channels')
  @RequirePermissions('update:subscriptions')
  @ApiOperation({ summary: 'Update channel config for a subscription' })
  @ApiAutoVoidResponse()
  async updateChannel(
    @Param('id') subscriptionId: string,
    @Body() body: UpdateChannelConfigRequestDto,
    @CurrentUser() user: AuthUser,
  ): Promise<void> {
    return this.commandBus.execute(
      new UpdateChannelConfigCommand(
        subscriptionId,
        requireUserId(user),
        body.channel,
        body.enabled,
        body.emailRole,
      ),
    );
  }

  @Get('resource')
  @RequirePermissions('read:subscriptions')
  @ApiOperation({ summary: 'List all subscribers for a resource (admin)' })
  @ApiAutoResponse(SubscriptionResponseDto, { isArray: true })
  async getResourceSubscribers(
    @Query('resourceType') resourceType: string,
    @Query('resourceId') resourceId?: string,
  ): Promise<SubscriptionResponseDto[]> {
    return this.queryBus.execute(new GetResourceSubscribersQuery(resourceType, resourceId));
  }
}
