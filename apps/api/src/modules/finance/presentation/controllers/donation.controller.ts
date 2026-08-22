import { Body, Controller, Get, HttpCode, HttpStatus, Param, Patch, Post, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiSecurity, ApiTags } from '@nestjs/swagger';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { CurrentUser, RequirePermissions, UnifiedAuthGuard, requireUserId } from '@nabarun-ngo/nestjs-shared-auth';
import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';
import { ApiAutoResponse, ApiPaginationQuery, ApiUuidParam } from '@nabarun-ngo/nestjs-shared-core';
import { CreateDonationCommand } from '../../application/commands/create-donation/create-donation.command';
import { CreateGuestDonorCommand } from '../../application/commands/create-guest-donor/create-guest-donor.command';
import { UpdateDonationCommand } from '../../application/commands/update-donation/update-donation.command';
import { ListDonationsQuery } from '../../application/queries/list-donations/list-donations.query';
import { GetDonationSummaryQuery } from '../../application/queries/get-donation-summary/get-donation-summary.query';
import { GetDonationReferenceDataQuery } from '../../application/queries/get-donation-reference-data/get-donation-reference-data.query';
import { DonationMapper } from '../../application/mappers/donation.mapper';
import { buildDonationDonorEnrichment } from '../../application/mappers/donation-donor-display.helper';
import { DonationType } from '../../domain/enums/donation-type.enum';
import { DonorType } from '../../domain/enums/donor-type.enum';
import { CreateDonationDto, CreateGuestDonationDto, DonationDetailFilterDto, DonationDto, DonationRefDataDto, DonationSummaryDto, UpdateDonationDto } from '../dtos/donation.dto';
import { DonationListResponseDto } from '../../application/dtos/donation-list.dto';

@ApiTags('Donation')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('donation')
export class DonationController {
  constructor(private readonly commandBus: CommandBus, private readonly queryBus: QueryBus) { }

  @Post('create')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:donation')
  @ApiAutoResponse(DonationDto, { status: HttpStatus.CREATED })
  async createDonation(@Body() dto: CreateDonationDto): Promise<DonationDto> {
    const donation = await this.commandBus.execute(
      new CreateDonationCommand({
        type: dto.type,
        amount: dto.amount,
        donorId: dto.donorId!,
        endDate: dto.endDate,
        startDate: dto.startDate,
        forEventId: dto.type === DonationType.ONETIME ? dto.forEventId : undefined,
      }),
    );
    return DonationMapper.toDto(donation);
  }

  @Post('create/guest')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:donation_guest')
  @ApiAutoResponse(DonationDto, { status: HttpStatus.CREATED })
  async createGuestDonation(@Body() dto: CreateGuestDonationDto): Promise<DonationDto> {
    const donor = await this.commandBus.execute(
      new CreateGuestDonorCommand({
        fullName: dto.donorName,
        email: dto.donorEmail,
        phoneNumber: dto.donorNumber,
      }),
    );
    const donation = await this.commandBus.execute(
      new CreateDonationCommand({
        type: DonationType.ONETIME,
        amount: dto.amount,
        donorId: donor.id,
        forEventId: dto.forEventId,
      }),
    );
    return DonationMapper.toDto(donation, buildDonationDonorEnrichment(donor));
  }

  @Patch(':id/update')
  @RequirePermissions('update:donation')
  @ApiUuidParam('id', 'Identifier of the donation')
  @ApiAutoResponse(DonationDto)
  async update(@Param('id') id: string, @Body() dto: UpdateDonationDto, @CurrentUser() user: AuthUser): Promise<DonationDto> {
    const donation = await this.commandBus.execute(
      new UpdateDonationCommand({
        id,
        status: dto.status,
        remarks: dto.remarks,
        amount: dto.amount,
        forEvent: dto.forEvent,
        paidToAccountId: dto.paidToAccountId,
        confirmedById: requireUserId(user),
        paidUsingUPI: dto.paidUsingUPI,
        paymentMethod: dto.paymentMethod,
        paidOn: dto.paidOn,
        isPaymentNotified: dto.isPaymentNotified,
      }),
    );
    return DonationMapper.toDto(donation);
  }

  @Get('static/referenceData')
  @ApiAutoResponse(DonationRefDataDto)
  getReferenceData(): Promise<DonationRefDataDto> {
    return this.queryBus.execute(new GetDonationReferenceDataQuery());
  }

  @Get('list/me')
  @ApiPaginationQuery()
  @ApiAutoResponse(DonationListResponseDto)
  getSelfDonations(
    @Query('pageIndex') pageIndex?: number,
    @Query('pageSize') pageSize?: number,
    @Query() filter?: DonationDetailFilterDto,
    @CurrentUser() user?: AuthUser,
  ): Promise<DonationListResponseDto> {
    return this.queryBus.execute(
      new ListDonationsQuery({ ...filter, userProfileId: user?.userId }, pageIndex, pageSize),
    );
  }

  @Get('list')
  @RequirePermissions('read:donations')
  @ApiPaginationQuery()
  @ApiAutoResponse(DonationListResponseDto)
  list(
    @Query('pageIndex') pageIndex?: number,
    @Query('pageSize') pageSize?: number,
    @Query() filter?: DonationDetailFilterDto,
  ): Promise<DonationListResponseDto> {
    return this.queryBus.execute(new ListDonationsQuery(filter ?? {}, pageIndex, pageSize));
  }

  @Get('list/guest')
  @RequirePermissions('read:donation_guest')
  @ApiPaginationQuery()
  @ApiAutoResponse(DonationListResponseDto)
  listGuest(
    @Query('pageIndex') pageIndex?: number,
    @Query('pageSize') pageSize?: number,
    @Query() filter?: DonationDetailFilterDto,
  ): Promise<DonationListResponseDto> {
    return this.queryBus.execute(
      new ListDonationsQuery({ ...filter, donorType: DonorType.GUEST }, pageIndex, pageSize),
    );
  }

  @Get('summary/me')
  @ApiAutoResponse(DonationSummaryDto)
  getSelfDonationSummary(@CurrentUser() user: AuthUser): Promise<DonationSummaryDto> {
    return this.queryBus.execute(new GetDonationSummaryQuery({ userProfileId: requireUserId(user) }));
  }

  @Get(':donorId/summary')
  @RequirePermissions('read:donations')
  @ApiUuidParam('donorId', 'Identifier of the donor')
  @ApiAutoResponse(DonationSummaryDto)
  getDonationSummary(@Param('donorId') donorId: string): Promise<DonationSummaryDto> {
    return this.queryBus.execute(new GetDonationSummaryQuery({ donorId }));
  }

  @Get(':memberId/list')
  @RequirePermissions('read:member_donations')
  @ApiUuidParam('memberId', 'Identifier of the member user profile')
  @ApiPaginationQuery()
  @ApiAutoResponse(DonationListResponseDto)
  getMemberDonations(
    @Param('memberId') memberId: string,
    @Query('pageIndex') pageIndex?: number,
    @Query('pageSize') pageSize?: number,
    @Query() filter?: DonationDetailFilterDto,
  ): Promise<DonationListResponseDto> {
    return this.queryBus.execute(
      new ListDonationsQuery({ ...filter, userProfileId: memberId }, pageIndex, pageSize),
    );
  }
}
