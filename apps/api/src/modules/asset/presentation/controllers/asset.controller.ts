import {
  Body,
  Controller,
  Delete,
  Get,
  HttpCode,
  HttpStatus,
  Param,
  Post,
  Put,
  Query,
  UseGuards,
} from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { CurrentUser, RequirePermissions, UnifiedAuthGuard, requireUserId } from '@nabarun-ngo/nestjs-shared-auth';
import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';
import {
  ApiAutoPagedResponse,
  ApiAutoResponse,
  ApiAutoVoidResponse,
  ApiPaginationQuery,
  ApiUuidParam,
  PagedResponse,
} from '@nabarun-ngo/nestjs-shared-core';
import { CreateAssetCommand } from '../../application/commands/create-asset/create-asset.command';
import { UpdateAssetCommand } from '../../application/commands/update-asset/update-asset.command';
import { DeleteAssetCommand } from '../../application/commands/delete-asset/delete-asset.command';
import { AssignAssetCustodyCommand } from '../../application/commands/assign-asset-custody/assign-asset-custody.command';
import { ReturnAssetCustodyCommand } from '../../application/commands/return-asset-custody/return-asset-custody.command';
import { ListAssetsQuery } from '../../application/queries/list-assets/list-assets.query';
import { GetAssetByIdQuery } from '../../application/queries/get-asset-by-id/get-asset-by-id.query';
import { AssetMapper } from '../../application/mappers/asset.mapper';
import {
  AssignAssetCustodyDto,
  AssetDetailDto,
  AssetDetailFilterDto,
  CreateAssetDto,
  ReturnAssetCustodyDto,
  UpdateAssetDto,
} from '../../application/dtos/asset.dto';

@ApiTags('Asset')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('assets')
export class AssetController {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) {}

  @Post('create')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:asset')
  @ApiAutoResponse(AssetDetailDto, { status: HttpStatus.CREATED })
  async createAsset(@Body() dto: CreateAssetDto, @CurrentUser() user: AuthUser): Promise<AssetDetailDto> {
    const asset = await this.commandBus.execute(
      new CreateAssetCommand({ ...dto, createdById: requireUserId(user) }),
    );
    return AssetMapper.toDto(asset);
  }

  @Get('list')
  @RequirePermissions('read:assets')
  @ApiPaginationQuery()
  @ApiAutoPagedResponse(AssetDetailDto)
  listAssets(
    @Query() filter?: AssetDetailFilterDto,
  ): Promise<PagedResponse<AssetDetailDto>> {
    return this.queryBus.execute(new ListAssetsQuery(filter));
  }

  @Get(':id')
  @RequirePermissions('read:assets')
  @ApiUuidParam('id', 'Identifier of the asset')
  @ApiAutoResponse(AssetDetailDto)
  getAssetById(@Param('id') id: string): Promise<AssetDetailDto> {
    return this.queryBus.execute(new GetAssetByIdQuery(id));
  }

  @Put('update/:id')
  @RequirePermissions('update:asset')
  @ApiUuidParam('id', 'Identifier of the asset')
  @ApiAutoResponse(AssetDetailDto)
  async updateAsset(
    @Param('id') id: string,
    @Body() dto: UpdateAssetDto,
    @CurrentUser() user: AuthUser,
  ): Promise<AssetDetailDto> {
    const asset = await this.commandBus.execute(
      new UpdateAssetCommand({ id, ...dto, updatedById: requireUserId(user) }),
    );
    return AssetMapper.toDto(asset);
  }

  @Post(':id/assign')
  @RequirePermissions('update:asset')
  @ApiUuidParam('id', 'Identifier of the asset')
  @ApiAutoResponse(AssetDetailDto)
  async assignCustody(
    @Param('id') id: string,
    @Body() dto: AssignAssetCustodyDto,
    @CurrentUser() user: AuthUser,
  ): Promise<AssetDetailDto> {
    const asset = await this.commandBus.execute(
      new AssignAssetCustodyCommand({
        id,
        custodianUserId: dto.custodianUserId,
        notes: dto.notes,
        assignedById: requireUserId(user),
      }),
    );
    return AssetMapper.toDto(asset);
  }

  @Post(':id/return')
  @RequirePermissions('update:asset')
  @ApiUuidParam('id', 'Identifier of the asset')
  @ApiAutoResponse(AssetDetailDto)
  async returnCustody(
    @Param('id') id: string,
    @Body() dto: ReturnAssetCustodyDto,
    @CurrentUser() user: AuthUser,
  ): Promise<AssetDetailDto> {
    const asset = await this.commandBus.execute(
      new ReturnAssetCustodyCommand({
        id,
        notes: dto.notes,
        returnedById: requireUserId(user),
      }),
    );
    return AssetMapper.toDto(asset);
  }

  @Delete(':id')
  @RequirePermissions('delete:asset')
  @ApiAutoVoidResponse({ status: HttpStatus.NO_CONTENT, description: 'Asset deleted' })
  @ApiUuidParam('id', 'Identifier of the asset')
  @HttpCode(HttpStatus.NO_CONTENT)
  deleteAsset(@Param('id') id: string): Promise<void> {
    return this.commandBus.execute(new DeleteAssetCommand(id));
  }
}
