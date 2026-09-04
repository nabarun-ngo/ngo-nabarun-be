import { Body, Controller, Get, HttpCode, HttpStatus, Param, Patch, Post, Put, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { RequirePermissions, UnifiedAuthGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoPagedResponse, ApiAutoResponse, ApiUuidParam, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { CreateBeneficiaryCommand } from '../../application/commands/create-beneficiary/create-beneficiary.command';
import { UpdateBeneficiaryCommand } from '../../application/commands/update-beneficiary/update-beneficiary.command';
import { ListBeneficiariesQuery } from '../../application/queries/list-beneficiaries/list-beneficiaries.query';
import { GetBeneficiaryByIdQuery } from '../../application/queries/get-beneficiary-by-id/get-beneficiary-by-id.query';
import { BeneficiaryMapper } from '../../application/mappers/beneficiary.mapper';
import { BeneficiaryDetailDto, BeneficiaryDetailFilterDto, CreateBeneficiaryDto, UpdateBeneficiaryDto } from '../../application/dtos/beneficiary.dto';

@ApiTags('Beneficiary')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('projects/:projectId/beneficiaries')
export class BeneficiaryController {
  constructor(private readonly commandBus: CommandBus, private readonly queryBus: QueryBus) { }
  @Get()
  @RequirePermissions('read:beneficiaries')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiAutoPagedResponse(BeneficiaryDetailDto)
  list(
    @Param('projectId') projectId: string,
    @Query() filter?: BeneficiaryDetailFilterDto,
  ): Promise<PagedResponse<BeneficiaryDetailDto>> {
    return this.queryBus.execute(new ListBeneficiariesQuery(projectId, filter));
  }
  @Post('create')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:beneficiary')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiAutoResponse(BeneficiaryDetailDto, { status: HttpStatus.CREATED })
  async create(@Param('projectId') projectId: string, @Body() dto: CreateBeneficiaryDto): Promise<BeneficiaryDetailDto> {
    const b = await this.commandBus.execute(new CreateBeneficiaryCommand({ ...dto, projectId }));
    return BeneficiaryMapper.toDto(b);
  }
  @Get(':id')
  @RequirePermissions('read:beneficiaries')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiUuidParam('id', 'Identifier of the beneficiary')
  @ApiAutoResponse(BeneficiaryDetailDto)
  getById(@Param('id') id: string): Promise<BeneficiaryDetailDto> {
    return this.queryBus.execute(new GetBeneficiaryByIdQuery(id));
  }
  @Put(':id/update')
  @RequirePermissions('update:beneficiary')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiUuidParam('id', 'Identifier of the beneficiary')
  @ApiAutoResponse(BeneficiaryDetailDto)
  async update(@Param('id') id: string, @Body() dto: UpdateBeneficiaryDto): Promise<BeneficiaryDetailDto> {
    const b = await this.commandBus.execute(new UpdateBeneficiaryCommand({ id, ...dto }));
    return BeneficiaryMapper.toDto(b);
  }
  @Patch(':id/exit')
  @RequirePermissions('update:beneficiary')
  @ApiUuidParam('projectId', 'Identifier of the parent project')
  @ApiUuidParam('id', 'Identifier of the beneficiary')
  @ApiAutoResponse(BeneficiaryDetailDto, { description: 'Beneficiary marked as exited from the project' })
  async exit(@Param('id') id: string): Promise<BeneficiaryDetailDto> {
    const b = await this.commandBus.execute(new UpdateBeneficiaryCommand({ id, exit: true }));
    return BeneficiaryMapper.toDto(b);
  }
}
