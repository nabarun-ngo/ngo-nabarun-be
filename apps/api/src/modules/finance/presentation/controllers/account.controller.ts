import { Body, Controller, Get, HttpStatus, Param, Post, Put, Query, UseGuards } from '@nestjs/common';
import {
  ApiBearerAuth,
  ApiCreatedResponse,
  ApiQuery,
  ApiSecurity,
  ApiTags,
  getSchemaPath,
} from '@nestjs/swagger';
import type { ApiResponseOptions } from '@nestjs/swagger';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { CurrentUser, RequirePermissions, UnifiedAuthGuard, requireUserId } from '@nabarun-ngo/nestjs-shared-auth';
import type { AuthUser } from '@nabarun-ngo/nestjs-shared-auth';
import {
  ApiAutoPagedResponse,
  ApiAutoPrimitiveResponse,
  ApiAutoResponse,
  ApiPaginationQuery,
  ApiUuidParam,
  PagedResponse,
  SuccessResponse,
} from '@nabarun-ngo/nestjs-shared-core';
import { CreateAccountCommand } from '../../application/commands/create-account/create-account.command';
import { UpdateAccountCommand } from '../../application/commands/update-account/update-account.command';
import { CreateTransactionCommand } from '../../application/commands/create-transaction/create-transaction.command';
import { ListAccountsQuery } from '../../application/queries/list-accounts/list-accounts.query';
import { ListAccountTransactionsQuery } from '../../application/queries/list-account-transactions/list-account-transactions.query';
import { GetPayableAccountsQuery } from '../../application/queries/get-payable-accounts/get-payable-accounts.query';
import { GetAccountReferenceDataQuery } from '../../application/queries/get-account-reference-data/get-account-reference-data.query';
import { GetIfscDetailsQuery } from '../../application/queries/get-ifsc-details/get-ifsc-details.query';
import { AccountMapper } from '../../application/mappers/account.mapper';
import { TransactionRefType } from '../../domain/enums/transaction.enum';
import { AccountDetailDto, AccountDetailFilterDto, AccountRefDataDto, CreateAccountDto, TransferDto, UpdateAccountDto, UpdateAccountSelfDto } from '../dtos/account.dto';
import { TransactionDetailDto, TransactionDetailFilterDto } from '../dtos/transaction.dto';
import { IfscDetailsDto } from '../dtos/ifsc.dto';

/**
 * A transfer answers with the bare transaction reference. `ApiAutoPrimitiveResponse`
 * cannot carry a sample value for a primitive payload, so the 201 is restated here
 * with the same envelope plus an example.
 */
const TRANSFER_CREATED_RESPONSE: ApiResponseOptions = {
  description: 'Identifier of the created transfer transaction',
  schema: {
    allOf: [
      { $ref: getSchemaPath(SuccessResponse) },
      { properties: { responsePayload: { type: 'string', example: 'TXR1234567890' } } },
    ],
  },
};

@ApiTags('Account')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('account')
export class AccountController {
  constructor(private readonly commandBus: CommandBus, private readonly queryBus: QueryBus) { }

  @Get('static/referenceData')
  @ApiAutoResponse(AccountRefDataDto)
  getAccountReferenceData(): Promise<AccountRefDataDto> {
    return this.queryBus.execute(new GetAccountReferenceDataQuery());
  }

  @Get('static/ifsc/:ifsc')
  @ApiAutoResponse(IfscDetailsDto)
  getIfscDetails(@Param('ifsc') ifsc: string): Promise<IfscDetailsDto> {
    return this.queryBus.execute(new GetIfscDetailsQuery(ifsc));
  }

  @Get('payable-account')
  @ApiQuery({
    name: 'reference',
    enum: ['ADHOC', 'ADVANCE_EV'],
    required: false,
    example: 'ADHOC',
    description:
      'Transfer reference. With fromAccountId, filters To accounts by From×Reference matrix. Omitted: BANK + ORG (donation/earning).',
  })
  @ApiQuery({
    name: 'fromAccountId',
    type: String,
    required: false,
    description: 'Source account id for transfer payable filtering',
  })
  @ApiQuery({
    name: 'purpose',
    enum: ['EARNING_INTEREST', 'DONATION', 'INVESTMENT_FUNDING'],
    required: false,
    description:
      'EARNING_INTEREST: ACTIVE BANK + INVESTMENT. INVESTMENT_FUNDING: ACTIVE BANK source accounts.',
  })
  @ApiAutoResponse(AccountDetailDto, { isArray: true })
  payableAccount(
    @Query('reference') reference?: 'ADHOC' | 'ADVANCE_EV',
    @Query('fromAccountId') fromAccountId?: string,
    @Query('purpose') purpose?: 'EARNING_INTEREST' | 'DONATION' | 'INVESTMENT_FUNDING',
  ): Promise<AccountDetailDto[]> {
    const normalizedReference =
      reference === 'ADHOC' || reference === 'ADVANCE_EV' ? reference : undefined;
    const normalizedPurpose =
      purpose === 'EARNING_INTEREST'
      || purpose === 'DONATION'
      || purpose === 'INVESTMENT_FUNDING'
        ? purpose
        : undefined;
    return this.queryBus.execute(
      new GetPayableAccountsQuery(
        normalizedReference,
        fromAccountId?.trim() || undefined,
        normalizedPurpose,
      ),
    );
  }

  @Get('list')
  @RequirePermissions('read:accounts')
  @ApiPaginationQuery()
  @ApiAutoPagedResponse(AccountDetailDto)
  listAccounts(
    @Query() filter?: AccountDetailFilterDto,
  ): Promise<PagedResponse<AccountDetailDto>> {
    return this.queryBus.execute(new ListAccountsQuery(filter));
  }

  @Get('list/me')
  @ApiPaginationQuery()
  @ApiAutoPagedResponse(AccountDetailDto)
  listSelfAccounts(
    @Query() filter?: AccountDetailFilterDto,
    @CurrentUser() user?: AuthUser,
  ): Promise<PagedResponse<AccountDetailDto>> {
    return this.queryBus.execute(new ListAccountsQuery(filter, user?.userId));
  }

  @Post('create')
  @RequirePermissions('create:account')
  @ApiAutoResponse(AccountDetailDto, { status: HttpStatus.CREATED })
  async createAccount(@Body() dto: CreateAccountDto): Promise<AccountDetailDto> {
    const account = await this.commandBus.execute(
      new CreateAccountCommand({
        name: dto.name,
        type: dto.type,
        ownerType: dto.ownerType,
        currency: dto.currency,
        description: dto.description,
        accountHolderId: dto.accountHolderId,
        custodianUserId: dto.custodianUserId,
        custodianUserIds: dto.custodianUserIds,
        bankDetail: dto.bankDetail,
        upiDetails: dto.upiDetails,
      }),
    );
    return AccountMapper.toDto(account, { includeBankDetail: true, includeUpiDetail: true });
  }

  @Put(':id/update')
  @RequirePermissions('update:account')
  @ApiUuidParam('id', 'Identifier of the account')
  @ApiAutoResponse(AccountDetailDto)
  async updateAccount(@Param('id') id: string, @Body() dto: UpdateAccountDto): Promise<AccountDetailDto> {
    const account = await this.commandBus.execute(
      new UpdateAccountCommand({
        id,
        name: dto.name,
        description: dto.description,
        accountStatus: dto.accountStatus,
        bankDetail: dto.bankDetail,
        upiDetail: dto.upiDetail,
        upiDetails: dto.upiDetails,
      }),
    );
    return AccountMapper.toDto(account, { includeBankDetail: true, includeUpiDetail: true });
  }

  @Put(':id/update/me')
  @ApiUuidParam('id', 'Identifier of the account')
  @ApiAutoResponse(AccountDetailDto)
  async updateSelf(@Param('id') id: string, @Body() dto: UpdateAccountSelfDto, @CurrentUser() user: AuthUser): Promise<AccountDetailDto> {
    const account = await this.commandBus.execute(
      new UpdateAccountCommand({
        id,
        description: dto.description,
        bankDetail: dto.bankDetail,
        upiDetail: dto.upiDetail,
        upiDetails: dto.upiDetails,
        actorUserId: requireUserId(user),
      }),
    );
    return AccountMapper.toDto(account, { includeBankDetail: true, includeUpiDetail: true });
  }

  @Get(':id/transactions')
  @RequirePermissions('read:transactions')
  @ApiUuidParam('id', 'Identifier of the account')
  @ApiPaginationQuery()
  @ApiAutoPagedResponse(TransactionDetailDto)
  listAccountTransactions(
    @Param('id') accountId: string,
    @Query() filter?: TransactionDetailFilterDto,
  ): Promise<PagedResponse<TransactionDetailDto>> {
    return this.queryBus.execute(new ListAccountTransactionsQuery(accountId, filter));
  }

  @Get(':id/transactions/me')
  @ApiUuidParam('id', 'Identifier of the account')
  @ApiPaginationQuery()
  @ApiAutoPagedResponse(TransactionDetailDto)
  listSelfAccountTransactions(
    @Param('id') accountId: string,
    @Query() filter?: TransactionDetailFilterDto,
    @CurrentUser() user?: AuthUser,
  ): Promise<PagedResponse<TransactionDetailDto>> {
    return this.queryBus.execute(new ListAccountTransactionsQuery(accountId, filter, user?.userId));
  }

  @Post(':id/transfer')
  @RequirePermissions('update:accounts', 'update:transactions')
  @ApiUuidParam('id', 'Identifier of the source account')
  @ApiCreatedResponse(TRANSFER_CREATED_RESPONSE)
  @ApiAutoPrimitiveResponse('string', {
    status: HttpStatus.CREATED,
    description: 'Identifier of the created transfer transaction',
  })
  transferAmount(
    @Param('id') accountId: string,
    @Body() dto: TransferDto,
    @CurrentUser() user: AuthUser,
  ): Promise<string> {
    return this.commandBus.execute(
      new CreateTransactionCommand({
        accountId,
        transferToAccountId: dto.toAccountId,
        transferReference: dto.reference,
        txnAmount: dto.amount,
        txnDescription: dto.description,
        txnDate: dto.transferDate,
        txnType: 'TRANSFER',
        currency: 'INR',
        txnRefType: TransactionRefType.NONE,
        actorUserId: requireUserId(user),
      }),
    );
  }

  @Post(':id/transfer/me')
  @ApiUuidParam('id', 'Identifier of the source account')
  @ApiCreatedResponse(TRANSFER_CREATED_RESPONSE)
  @ApiAutoPrimitiveResponse('string', {
    status: HttpStatus.CREATED,
    description: 'Identifier of the created transfer transaction',
  })
  transferAmountSelf(@Param('id') accountId: string, @Body() dto: TransferDto, @CurrentUser() user: AuthUser): Promise<string> {
    return this.commandBus.execute(
      new CreateTransactionCommand({
        accountId,
        transferToAccountId: dto.toAccountId,
        transferReference: dto.reference,
        txnAmount: dto.amount,
        txnDescription: dto.description,
        txnDate: dto.transferDate,
        txnType: 'TRANSFER',
        currency: 'INR',
        txnRefType: TransactionRefType.NONE,
        actorUserId: requireUserId(user),
      }),
    );
  }
}

