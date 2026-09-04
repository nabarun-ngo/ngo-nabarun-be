import { Body, Controller, Get, HttpCode, HttpStatus, Param, Post, Put, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { CurrentUser, RequirePermissions, UnifiedAuthGuard, requireUserId } from '@nabarun-ngo/nestjs-shared-auth';
import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoPagedResponse, ApiAutoResponse, ApiUuidParam, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { CreateEarningCommand } from '../../application/commands/create-earning/create-earning.command';
import { UpdateEarningCommand } from '../../application/commands/update-earning/update-earning.command';
import { ListEarningsQuery } from '../../application/queries/list-earnings/list-earnings.query';
import { GetEarningByIdQuery } from '../../application/queries/get-earning-by-id/get-earning-by-id.query';
import { GetEarningReferenceDataQuery } from '../../application/queries/get-earning-reference-data/get-earning-reference-data.query';
import { EarningMapper } from '../../application/mappers/earning.mapper';
import { CreateEarningDto, EarningDetailDto, EarningDetailFilterDto, EarningRefDataDto, UpdateEarningDto } from '../dtos/earning.dto';

@ApiTags('Earning')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('earning')
export class EarningController {
  constructor(private readonly commandBus: CommandBus, private readonly queryBus: QueryBus) { }

  @Get('static/referenceData')
  @ApiAutoResponse(EarningRefDataDto)
  getEarningReferenceData(): Promise<EarningRefDataDto> {
    return this.queryBus.execute(new GetEarningReferenceDataQuery());
  }

  @Post('create')
  @HttpCode(HttpStatus.OK)
  @RequirePermissions('create:earning')
  @ApiAutoResponse(EarningDetailDto)
  async createEarning(@Body() dto: CreateEarningDto, @CurrentUser() user: AuthUser): Promise<EarningDetailDto> {
    const earning = await this.commandBus.execute(
      new CreateEarningCommand({
        userId: requireUserId(user),
        category: dto.category,
        amount: dto.amount,
        currency: dto.currency,
        source: dto.source,
        description: dto.description,
        accountId: dto.accountId,
      }),
    );
    return EarningMapper.toDto(earning);
  }

  @Put(':id/update')
  @RequirePermissions('update:earning')
  @ApiUuidParam('id', 'Identifier of the earning')
  @ApiAutoResponse(EarningDetailDto)
  async updateEarning(@Param('id') id: string, @Body() dto: UpdateEarningDto, @CurrentUser() user: AuthUser): Promise<EarningDetailDto> {
    const earning = await this.commandBus.execute(
      new UpdateEarningCommand({
        id,
        userId: requireUserId(user),
        category: dto.category,
        amount: dto.amount,
        description: dto.description,
        source: dto.source,
        earningDate: dto.earningDate,
        status: dto.status,
        accountId: dto.accountId,
      }),
    );
    return EarningMapper.toDto(earning);
  }

  @Get('list')
  @RequirePermissions('read:earnings')
  @ApiAutoPagedResponse(EarningDetailDto)
  listEarnings(
    @Query() filter?: EarningDetailFilterDto,
  ): Promise<PagedResponse<EarningDetailDto>> {
    return this.queryBus.execute(new ListEarningsQuery(filter));
  }

  @Get(':id')
  @RequirePermissions('read:earnings')
  @ApiUuidParam('id', 'Identifier of the earning')
  @ApiAutoResponse(EarningDetailDto)
  getEarningById(@Param('id') id: string): Promise<EarningDetailDto> {
    return this.queryBus.execute(new GetEarningByIdQuery(id));
  }
}

