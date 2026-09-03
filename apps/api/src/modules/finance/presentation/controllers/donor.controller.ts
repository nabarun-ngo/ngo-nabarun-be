import { Body, Controller, Get, HttpCode, HttpStatus, Param, Patch, Post, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { RequirePermissions, UnifiedAuthGuard } from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoPagedResponse, ApiAutoResponse, ApiUuidParam, PagedResponse } from '@nabarun-ngo/nestjs-shared-core';
import { CreateGuestDonorCommand } from '../../application/commands/create-guest-donor/create-guest-donor.command';
import { UpdateGuestDonorCommand } from '../../application/commands/update-guest-donor/update-guest-donor.command';
import { UpdateMemberDonorCommand } from '../../application/commands/update-member-donor/update-member-donor.command';
import { MergeGuestDonorsCommand } from '../../application/commands/merge-guest-donors/merge-guest-donors.command';
import { ListDonorsQuery } from '../../application/queries/list-donors/list-donors.query';
import { GetDonorByIdQuery } from '../../application/queries/get-donor-by-id/get-donor-by-id.query';
import { GetDonorReferenceDataQuery } from '../../application/queries/get-donor-reference-data/get-donor-reference-data.query';
import { DonorMapper } from '../../application/mappers/donor.mapper';
import {
  CreateGuestDonorRequestDto,
  DonorRefDataDto,
  DonorResponseDto,
  ListDonorsQueryDto,
  MergeGuestDonorsRequestDto,
  UpdateGuestDonorRequestDto,
  UpdateMemberDonorRequestDto,
} from '../dtos/donor.dto';

@ApiTags('Donor')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('donor')
export class DonorController {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) {}

  @Post('guest')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:donor_guest')
  @ApiAutoResponse(DonorResponseDto, { status: HttpStatus.CREATED })
  async createGuest(@Body() dto: CreateGuestDonorRequestDto): Promise<DonorResponseDto> {
    const donor = await this.commandBus.execute(new CreateGuestDonorCommand(dto));
    return DonorMapper.toDto(donor);
  }

  @Patch('guest/:id')
  @RequirePermissions('update:donor_guest')
  @ApiUuidParam('id', 'Guest donor identifier')
  @ApiAutoResponse(DonorResponseDto)
  async updateGuest(
    @Param('id') id: string,
    @Body() dto: UpdateGuestDonorRequestDto,
  ): Promise<DonorResponseDto> {
    const donor = await this.commandBus.execute(
      new UpdateGuestDonorCommand({ donorId: id, ...dto }),
    );
    return DonorMapper.toDto(donor);
  }

  @Post('guest/merge')
  @RequirePermissions('merge:donor_guest')
  @ApiAutoResponse(DonorResponseDto)
  async mergeGuest(@Body() dto: MergeGuestDonorsRequestDto): Promise<DonorResponseDto> {
    const donor = await this.commandBus.execute(new MergeGuestDonorsCommand(dto));
    return DonorMapper.toDto(donor);
  }

  @Get('list')
  @RequirePermissions('read:donors')
  @ApiAutoPagedResponse(DonorResponseDto)
  list(@Query() query: ListDonorsQueryDto): Promise<PagedResponse<DonorResponseDto>> {
    return this.queryBus.execute(
      new ListDonorsQuery(
        { q: query.q, type: query.type, status: query.status },
        query.pageIndex,
        query.pageSize,
        query.sortBy,
        query.sortDir,
      ),
    );
  }

  @Get('static/referenceData')
  @ApiAutoResponse(DonorRefDataDto)
  getDonorReferenceData(): Promise<DonorRefDataDto> {
    return this.queryBus.execute(new GetDonorReferenceDataQuery());
  }

  @Get(':id')
  @RequirePermissions('read:donors')
  @ApiUuidParam('id', 'Donor identifier')
  @ApiAutoResponse(DonorResponseDto)
  getById(@Param('id') id: string): Promise<DonorResponseDto> {
    return this.queryBus.execute(new GetDonorByIdQuery(id));
  }

  @Patch('member/:id')
  @RequirePermissions('update:donor_member')
  @ApiUuidParam('id', 'Member donor identifier')
  @ApiAutoResponse(DonorResponseDto)
  async updateMember(
    @Param('id') id: string,
    @Body() dto: UpdateMemberDonorRequestDto,
  ): Promise<DonorResponseDto> {
    const donor = await this.commandBus.execute(
      new UpdateMemberDonorCommand({ donorId: id, ...dto }),
    );
    return DonorMapper.toDto(donor);
  }
}
