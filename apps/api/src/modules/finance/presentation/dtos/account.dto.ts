import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsString, IsOptional, IsNumber, IsEnum, ValidateNested, IsDate, IsArray, IsBoolean } from 'class-validator';
import { Transform, Type } from 'class-transformer';
import { AccountStatus } from '../../domain/enums/account-status.enum';
import { AccountOwnerType } from '../../domain/enums/account-owner-type.enum';
import { AccountType } from '../../domain/enums/account-type.enum';
import { KeyValueOption } from '../../application/ports/finance-reference-data.port';
import { PaginatedQueryDto } from '@nabarun-ngo/nestjs-shared-core';

export class BankDetailDto {
  @ApiPropertyOptional({ example: 'Asha Verma' })
  @IsOptional()
  @IsString()
  bankAccountHolderName?: string;

  @ApiPropertyOptional({ example: 'HDFC Bank' })
  @IsOptional()
  @IsString()
  bankName?: string;

  @ApiPropertyOptional({ example: 'Salt Lake Sector V' })
  @IsOptional()
  @IsString()
  bankBranch?: string;

  @ApiPropertyOptional({ example: '50100234567890' })
  @IsOptional()
  @IsString()
  bankAccountNumber?: string;

  @ApiPropertyOptional({ example: 'SAVINGS' })
  @IsOptional()
  @IsString()
  bankAccountType?: string;

  @ApiPropertyOptional({ example: 'HDFC0001234' })
  @IsOptional()
  @IsString()
  IFSCNumber?: string;

  @ApiPropertyOptional({ example: '2027-03-14' })
  @IsOptional()
  @IsString()
  maturityDate?: string;

  @ApiPropertyOptional({ example: 150000 })
  @IsOptional()
  @IsNumber()
  maturityAmount?: number;

  @ApiPropertyOptional({ example: 100000 })
  @IsOptional()
  @IsNumber()
  investmentAmount?: number;

  @ApiPropertyOptional({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  @IsOptional()
  @IsString()
  sourceAccountId?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  dematId?: string;

  @ApiPropertyOptional({ example: 7.5 })
  @IsOptional()
  @IsNumber()
  interestRate?: number;

  @ApiPropertyOptional({ example: 'QUARTERLY' })
  @IsOptional()
  @IsString()
  interestPayingTerm?: string;
}

export class UPIDetailDto {
  @ApiPropertyOptional({ example: 'upi-primary' })
  @IsOptional()
  @IsString()
  id?: string;

  @ApiPropertyOptional({ example: 'Asha Verma' })
  @IsOptional()
  @IsString()
  payeeName?: string;

  @ApiPropertyOptional({ example: 'nabarun@hdfcbank' })
  @IsOptional()
  @IsString()
  upiId?: string;

  @ApiPropertyOptional({ example: '+919876543210' })
  @IsOptional()
  @IsString()
  mobileNumber?: string;

  @ApiPropertyOptional({ example: 'upi://pay?pa=nabarun@hdfcbank&pn=Asha%20Verma&cu=INR' })
  @IsOptional()
  @IsString()
  qrData?: string;

  @ApiPropertyOptional({ example: 'Primary HDFC UPI' })
  @IsOptional()
  @IsString()
  label?: string;

  @ApiPropertyOptional({ example: true })
  @IsOptional()
  @IsBoolean()
  isPrimary?: boolean;
}

export class AccountDetailDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  id: string;

  @ApiPropertyOptional({ example: 'Asha Verma' })
  accountHolderName?: string;

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  accountHolder?: string;

  @ApiPropertyOptional({ enum: AccountOwnerType, example: AccountOwnerType.INDIVIDUAL })
  ownerType?: AccountOwnerType;

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41', deprecated: true })
  custodianUserId?: string;

  @ApiPropertyOptional({
    type: [String],
    example: ['b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41', 'c52e8f71-0d49-5b26-9g38-7e1f3b0c4d52'],
  })
  custodianUserIds?: string[];

  @ApiPropertyOptional({ example: 25000 })
  balance?: number;

  @ApiProperty({ enum: AccountStatus, example: AccountStatus.ACTIVE })
  accountStatus: AccountStatus;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  activatedOn?: Date;

  @ApiProperty({ enum: AccountType, example: AccountType.BANK })
  accountType: AccountType;

  @ApiPropertyOptional({ type: BankDetailDto })
  @IsOptional()
  @ValidateNested()
  @Type(() => BankDetailDto)
  bankDetail?: BankDetailDto;

  @ApiPropertyOptional({ type: UPIDetailDto, deprecated: true })
  @IsOptional()
  @ValidateNested()
  @Type(() => UPIDetailDto)
  upiDetail?: UPIDetailDto;

  @ApiPropertyOptional({ type: [UPIDetailDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UPIDetailDto)
  upiDetails?: UPIDetailDto[];
}

export class AccountDetailFilterDto extends PaginatedQueryDto {
  @ApiPropertyOptional({ enum: AccountStatus, isArray: true, example: [AccountStatus.ACTIVE] })
  @IsOptional()
  @Transform(({ value }) =>
    Array.isArray(value) ? value : value ? [value] : undefined
  )
  status?: AccountStatus[];

  @ApiPropertyOptional({ enum: AccountType, isArray: true, example: [AccountType.BANK, AccountType.WALLET] })
  @IsOptional()
  @Transform(({ value }) =>
    Array.isArray(value) ? value : value ? [value] : undefined
  )
  type?: AccountType[];

  @ApiPropertyOptional({ enum: AccountOwnerType, isArray: true, example: [AccountOwnerType.ORG] })
  @IsOptional()
  @Transform(({ value }) =>
    Array.isArray(value) ? value : value ? [value] : undefined
  )
  ownerType?: AccountOwnerType[];

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @IsOptional()
  @IsString()
  accountHolderId?: string;

  @ApiPropertyOptional({ example: 'Asha Verma' })
  @IsOptional()
  @IsString()
  accountHolderName?: string;

  @ApiPropertyOptional({ enum: ['Y', 'N'], example: 'Y' })
  @IsOptional()
  @IsEnum(['Y', 'N'])
  includePaymentDetail?: 'Y' | 'N';

  @ApiPropertyOptional({ enum: ['Y', 'N'], example: 'Y' })
  @IsOptional()
  @IsEnum(['Y', 'N'])
  includeBalance?: 'Y' | 'N';

  @ApiPropertyOptional({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  @IsOptional()
  @IsString()
  accountId?: string;
}

export class CreateAccountDto {
  @ApiProperty({ example: 'Nabarun Donation Account' })
  @IsString()
  name: string;

  @ApiProperty({ enum: AccountType, example: AccountType.BANK })
  @IsEnum(AccountType)
  type: AccountType;

  @ApiProperty({ enum: AccountOwnerType, example: AccountOwnerType.INDIVIDUAL })
  @IsEnum(AccountOwnerType)
  ownerType: AccountOwnerType;

  @ApiProperty({ example: 'INR' })
  @IsString()
  currency: string;

  @ApiPropertyOptional({ example: 'Primary account for collecting monthly donations' })
  @IsOptional()
  @IsString()
  description?: string;

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @IsOptional()
  @IsString()
  accountHolderId?: string;

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41', deprecated: true })
  @IsOptional()
  @IsString()
  custodianUserId?: string;

  @ApiPropertyOptional({
    type: [String],
    example: ['b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41', 'c52e8f71-0d49-5b26-9g38-7e1f3b0c4d52'],
  })
  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  custodianUserIds?: string[];

  @ApiPropertyOptional({ type: BankDetailDto })
  @IsOptional()
  @ValidateNested()
  @Type(() => BankDetailDto)
  bankDetail?: BankDetailDto;

  @ApiPropertyOptional({ type: [UPIDetailDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UPIDetailDto)
  upiDetails?: UPIDetailDto[];
}

export class UpdateAccountSelfDto {
  @ApiPropertyOptional({ example: 'Primary account for collecting monthly donations' })
  @IsOptional()
  @IsString()
  description?: string;

  @ApiPropertyOptional({ type: BankDetailDto })
  @IsOptional()
  @ValidateNested()
  @Type(() => BankDetailDto)
  bankDetail?: BankDetailDto;

  @ApiPropertyOptional({ type: UPIDetailDto, deprecated: true })
  @IsOptional()
  @ValidateNested()
  @Type(() => UPIDetailDto)
  upiDetail?: UPIDetailDto;

  @ApiPropertyOptional({ type: [UPIDetailDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UPIDetailDto)
  upiDetails?: UPIDetailDto[];
}

export class UpdateAccountDto {
  @ApiPropertyOptional({ example: 'Nabarun Donation Account' })
  @IsOptional()
  @IsString()
  name?: string;

  @ApiPropertyOptional({ enum: AccountStatus, example: AccountStatus.ACTIVE })
  @IsOptional()
  @IsEnum(AccountStatus)
  accountStatus?: AccountStatus;

  @ApiPropertyOptional({ example: 'Primary account for collecting monthly donations' })
  @IsOptional()
  @IsString()
  description?: string;

  @ApiPropertyOptional({ type: BankDetailDto })
  @IsOptional()
  @ValidateNested()
  @Type(() => BankDetailDto)
  bankDetail?: BankDetailDto;

  @ApiPropertyOptional({ type: UPIDetailDto, deprecated: true })
  @IsOptional()
  @ValidateNested()
  @Type(() => UPIDetailDto)
  upiDetail?: UPIDetailDto;

  @ApiPropertyOptional({ type: [UPIDetailDto] })
  @IsOptional()
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => UPIDetailDto)
  upiDetails?: UPIDetailDto[];
}

export class TransferDto {
  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsString()
  toAccountId: string;

  @ApiProperty({ example: 25000 })
  @IsNumber()
  amount: number;

  @ApiProperty({ example: 'Transfer to general account for literacy drive' })
  @IsString()
  description: string;

  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' })
  @IsDate()
  transferDate: Date;

  @ApiProperty({ enum: ['ADHOC', 'ADVANCE_EV'], example: 'ADHOC' })
  @IsEnum(['ADHOC', 'ADVANCE_EV'])
  reference: 'ADHOC' | 'ADVANCE_EV';
}

export class AccountStatusGroupsDto {
  @ApiProperty({ type: [String], example: ['ACTIVE'] })
  outstanding: string[];

  @ApiProperty({ type: [String], example: ['CLOSED'] })
  closed: string[];

  @ApiProperty({ type: [String], example: [] })
  excluded: string[];
}

export class TransferMatrixRowDto {
  @ApiProperty({ example: 'BANK' })
  fromAccountType: string;

  @ApiProperty({ example: 'ADHOC' })
  reference: string;

  @ApiProperty({ type: [String], example: ['BANK', 'WALLET'] })
  toAccountTypes: string[];
}

export class AccountRefDataDto {
  @ApiProperty({ example: [{ key: 'ACTIVE', value: 'Active' }, { key: 'CLOSED', value: 'Closed' }] })
  accountStatuses?: KeyValueOption[];

  @ApiPropertyOptional({ type: () => AccountStatusGroupsDto })
  accountStatusGroups?: AccountStatusGroupsDto;

  @ApiProperty({ example: [{ key: 'BANK', value: 'Bank Account' }, { key: 'WALLET', value: 'Wallet' }] })
  accountTypes?: KeyValueOption[];

  @ApiProperty({ example: [{ key: 'ORG', value: 'Organization' }, { key: 'INDIVIDUAL', value: 'Individual' }] })
  ownerTypes?: KeyValueOption[];

  @ApiProperty({ example: [{ key: 'Savings', value: 'Savings' }, { key: 'Current', value: 'Current' }] })
  bankAccountTypes?: KeyValueOption[];

  @ApiProperty({ example: [{ key: 'FD', value: 'Fixed Deposit' }, { key: 'MF', value: 'Mutual Fund' }] })
  investmentTypes?: KeyValueOption[];

  @ApiProperty({ example: [{ key: 'MONTHLY', value: 'Monthly' }, { key: 'QUARTERLY', value: 'Quarterly' }] })
  interestPayingTerms?: KeyValueOption[];

  @ApiProperty({ example: [{ key: 'ADHOC', value: 'General' }, { key: 'ADVANCE_EV', value: 'Advance for Event' }] })
  transferReferenceTypes?: KeyValueOption[];

  @ApiPropertyOptional({ type: () => [TransferMatrixRowDto] })
  transferMatrix?: TransferMatrixRowDto[];

  @ApiProperty({ example: [{ key: 'IN', value: 'Credit (IN)' }, { key: 'OUT', value: 'Debit (OUT)' }] })
  transactionTypes?: KeyValueOption[];

  @ApiProperty({ example: [{ key: 'SUCCESS', value: 'Success' }, { key: 'REVERSED', value: 'Reversed' }] })
  transactionStatuses?: KeyValueOption[];

  @ApiProperty({ example: [{ key: 'DONATION', value: 'Donation' }, { key: 'EXPENSE', value: 'Expense' }] })
  transactionRefTypes?: KeyValueOption[];
}

export class AddFundDto {
  @ApiProperty({ example: 25000 })
  @IsNumber()
  amount: number;

  @ApiProperty({ example: 'Opening fund for the donation account' })
  @IsString()
  description: string;

  @ApiProperty({ example: '2026-03-14T09:30:00.000Z' })
  @IsDate()
  transferDate: Date;
}

export class FixTransactionDto {
  @ApiProperty({ example: ['3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55'] })
  @IsArray()
  itemIds: string[];

  @ApiProperty({ enum: ['EXPENSE', 'DONATION'], example: 'EXPENSE' })
  @IsEnum(['EXPENSE', 'DONATION'])
  itemType: 'EXPENSE' | 'DONATION';

  @ApiProperty({ example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsString()
  newAccountId: string;
}
