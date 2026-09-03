import { IsNumber, IsString, IsOptional, Min, IsEmail, IsBoolean, IsDate, IsArray, IsEnum } from 'class-validator';
import { Transform, Type } from 'class-transformer';
import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { DonationStatus } from '../../domain/enums/donation-status.enum';
import { DonationType } from '../../domain/enums/donation-type.enum';
import { PaymentMethod } from '../../domain/enums/payment-method.enum';
import { UPIPaymentType } from '../../domain/enums/upi-payment-type.enum';
import { AccountDetailDto } from './account.dto';
import { FinanceUserDto } from '../../application/dtos/finance-user.dto';
import { InvoiceSummaryDto } from '../../../invoice/application/dtos/invoice.dto';
import { PaginatedQueryDto } from '@nabarun-ngo/nestjs-shared-core';

export class CreateDonationDto {

  @IsEnum(DonationType)
  @ApiProperty({ enum: DonationType, example: DonationType.REGULAR })
  type: DonationType;


  @IsNumber()
  @Min(1)
  @ApiProperty({ minimum: 1, example: 1500 })
  amount: number;


  @IsDate()
  @Type(() => Date)
  @IsOptional()
  @ApiPropertyOptional({ type: String, format: 'date-time', description: 'Start date for regular donations', example: '2026-03-14T09:30:00.000Z' })
  startDate?: Date;

  @IsDate()
  @Type(() => Date)
  @IsOptional()
  @ApiPropertyOptional({ type: String, format: 'date-time', description: 'End date for regular donations', example: '2026-12-31T23:59:59.000Z' })
  endDate?: Date;

  @IsOptional()
  @IsString()
  @ApiPropertyOptional({ description: 'Required in case of ONETIME donations', example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  forEventId?: string;

  @IsOptional()
  @IsString()
  @ApiPropertyOptional({ description: 'Required in case of ONETIME donations', example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  donorId?: string;

}

export class CreateGuestDonationDto {
  @IsNumber()
  @Min(1)
  @ApiProperty({ minimum: 1, example: 2500 })
  amount: number;

  @IsOptional()
  @IsString()
  @ApiPropertyOptional({ description: 'Optional for guests', example: '+919876543210' })
  donorNumber?: string; // Optional for internal members

  @IsOptional()
  @IsString()
  @ApiPropertyOptional({ description: 'Required for guests', example: 'Asha Verma' })
  donorName: string; // Required for guests

  @IsOptional()
  @IsEmail()
  @ApiPropertyOptional({ description: 'Optional for guests', example: 'asha.verma@example.org' })
  donorEmail?: string; // Required for guests

  @IsOptional()
  @IsString()
  @ApiPropertyOptional({ description: 'Required in case of ONETIME donations', example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  forEventId?: string;
}

export class DonationDetailFilterDto extends PaginatedQueryDto {
  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @IsOptional()
  @IsString()
  donorId?: string;

  @ApiPropertyOptional({ enum: DonationStatus, isArray: true, example: [DonationStatus.RAISED, DonationStatus.PAID] })
  @IsOptional()
  @IsArray()
  @Transform(({ value }) =>
    Array.isArray(value) ? value : value ? [value] : undefined
  )
  status?: DonationStatus[];

  @ApiPropertyOptional({ enum: DonationType, isArray: true, example: [DonationType.REGULAR] })
  @IsOptional()
  @IsArray()
  @Transform(({ value }) =>
    Array.isArray(value) ? value : value ? [value] : undefined
  )
  type?: DonationType[];

  @ApiPropertyOptional({ enum: ['Y', 'N'], example: 'N' })
  @IsOptional()
  @IsEnum(['Y', 'N'])
  isGuest?: 'Y' | 'N';

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  startDate?: Date;

  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-31T23:59:59.000Z' })
  @IsOptional()
  @IsDate()
  @Type(() => Date)
  endDate?: Date;

  @ApiPropertyOptional({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  @IsOptional()
  @IsString()
  donationId?: string;

  @ApiPropertyOptional({ example: 'Asha Verma' })
  @IsOptional()
  @IsString()
  donorName?: string;

  @ApiPropertyOptional({ description: 'Activity ID this donation is for', example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  @IsOptional()
  @IsString()
  forEventId?: string;

}

export class DownloadDonationSummaryDto {
  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  @IsDate()
  @Type(() => Date)
  startDate: Date;

  @ApiProperty({ type: String, format: 'date-time', example: '2026-03-31T23:59:59.000Z' })
  @IsDate()
  @Type(() => Date)
  endDate: Date;
}

export class DonationDto {
  @ApiProperty({ example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  id: string;

  @ApiPropertyOptional({ description: 'Whether this is a guest donation', example: false })
  isGuest: boolean;

  @ApiProperty({ example: 1500 })
  amount: number;

  @ApiProperty({ example: 'INR' })
  currency: string;

  @ApiProperty({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  donorId: string;

  @ApiProperty({ example: 'Asha Verma' })
  donorName: string;

  @ApiPropertyOptional({ example: 'asha.verma@example.org' })
  donorEmail?: string;

  @ApiPropertyOptional({ example: '+919876543210' })
  donorNumber?: string;

  @ApiPropertyOptional({ type: String, format: 'date-time', description: 'Start date for regular donations', example: '2026-03-14T09:30:00.000Z' })
  startDate?: Date;

  @ApiPropertyOptional({ type: String, format: 'date-time', description: 'End date for regular donations', example: '2026-12-31T23:59:59.000Z' })
  endDate?: Date;

  @ApiProperty({ type: String, format: 'date-time', description: 'Date when donation was raised', example: '2026-03-14T09:30:00.000Z' })
  raisedOn: Date;

  @ApiProperty({ enum: DonationType, description: 'Donation type: REGULAR or ONETIME', example: DonationType.REGULAR })
  type: DonationType;

  @ApiProperty({ enum: DonationStatus, example: DonationStatus.PAID })
  status: DonationStatus;

  @ApiPropertyOptional({ type: String, format: 'date-time', description: 'Date when donation was paid', example: '2026-03-14T11:45:00.000Z' })
  paidOn?: Date;

  @ApiPropertyOptional({ description: 'User ID who confirmed the donation' })
  confirmedBy?: FinanceUserDto;

  @ApiPropertyOptional({ type: String, format: 'date-time', description: 'Date when donation was confirmed', example: '2026-03-14T14:20:00.000Z' })
  confirmedOn?: Date;

  @ApiPropertyOptional({ enum: PaymentMethod, description: 'Payment method used', example: PaymentMethod.UPI })
  paymentMethod?: PaymentMethod;

  @ApiPropertyOptional({ description: 'Account ID where payment was made' })
  paidToAccount?: AccountDetailDto;

  @ApiPropertyOptional({ description: 'Event ID this donation is for', example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  forEvent?: string; // EventDetail reference

  @ApiPropertyOptional({ enum: UPIPaymentType, description: 'UPI payment type if payment method is UPI', example: UPIPaymentType.GPAY })
  paidUsingUPI?: UPIPaymentType;

  @ApiPropertyOptional({ description: 'Whether payment notification was sent', example: true })
  isPaymentNotified?: boolean;

  @ApiPropertyOptional({ description: 'Transaction reference ID', example: 'TXN-2026-000482' })
  transactionRef?: string;

  @ApiPropertyOptional({ description: 'Additional remarks', example: 'Monthly donation' })
  remarks?: string;

  @ApiPropertyOptional({ description: 'Reason for cancellation (legacy typo preserved)', example: 'Donor requested cancellation' })
  cancelletionReason?: string;

  @ApiPropertyOptional({ description: 'Reason for later payment', example: 'Donor will pay after salary credit' })
  laterPaymentReason?: string;

  @ApiPropertyOptional({ description: 'Payment failure details', example: 'UPI collect request expired' })
  paymentFailureDetail?: string;

  @ApiProperty({ description: 'Next possible statuses for this donation', isArray: true, enum: DonationStatus, example: [DonationStatus.CANCELLED, DonationStatus.UPDATE_MISTAKE] })
  @IsArray()
  @IsEnum(DonationStatus, { each: true })
  nextStatuses: DonationStatus[];

  @ApiPropertyOptional({ description: 'Activity ID this donation is for', example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  activityId?: string;

  @ApiPropertyOptional({ description: 'Activity name this donation is for', example: 'Literacy Drive 2026' })
  activityName?: string;

  @ApiPropertyOptional({ description: 'Issued donation receipt' })
  invoice?: InvoiceSummaryDto;

}

export class UpdateDonationDto {
  @IsOptional()
  @IsEnum(DonationStatus)
  @ApiPropertyOptional({ enum: DonationStatus, example: DonationStatus.PAID })
  status?: DonationStatus;

  @IsOptional()
  @IsEnum(PaymentMethod)
  @ApiPropertyOptional({ enum: PaymentMethod, example: PaymentMethod.UPI })
  paymentMethod?: PaymentMethod;

  @IsOptional()
  @IsEnum(UPIPaymentType)
  @ApiPropertyOptional({ enum: UPIPaymentType, example: UPIPaymentType.GPAY })
  paidUsingUPI?: UPIPaymentType;

  @IsOptional()
  @IsString()
  @ApiPropertyOptional({ description: 'Additional remarks', example: 'Monthly donation' })
  remarks?: string;

  @IsOptional()
  @IsNumber()
  @Min(1)
  @ApiPropertyOptional({ description: 'Amount', minimum: 1, example: 1500 })
  amount?: number;

  @IsOptional()
  @IsString()
  @ApiPropertyOptional({ description: 'Event ID this donation is for', example: '7c2e5b84-13af-4d6c-8e90-5a1f3b2c7d68' })
  forEvent?: string;

  @IsOptional()
  @IsString()
  @ApiPropertyOptional({ description: 'Account ID where payment was made', example: '3f8a1c92-5d47-4e0b-9a6f-2b7c8e1d4a55' })
  paidToAccountId?: string;

  @IsOptional()
  @IsString()
  @ApiPropertyOptional({ example: 'TXN-2026-000482' })
  transactionRef?: string;

  @IsOptional()
  @IsBoolean()
  @ApiPropertyOptional({ example: true })
  isPaymentNotified?: boolean;

  @IsOptional()
  @IsDate()
  @Type(() => Date)
  @ApiPropertyOptional({ type: String, format: 'date-time', example: '2026-03-14T09:30:00.000Z' })
  paidOn?: Date;

}


export class DonationSummaryDto {
  @ApiProperty({ example: true })
  @IsBoolean()
  hasOutstanding: boolean;

  @ApiProperty({ example: ['2026-01', '2026-02'] })
  @IsArray()
  outstandingMonths: string[];

  @ApiProperty({ example: 3000 })
  @IsNumber()
  outstandingAmount: number;
}

export class DonationReferenceOptionDto {
  @ApiProperty()
  key: string;

  @ApiProperty()
  value: string;

  @ApiPropertyOptional()
  description?: string;
}

export class DonationStatusGroupsDto {
  @ApiProperty({ type: [String] })
  outstanding: string[];

  @ApiProperty({ type: [String] })
  closed: string[];

  @ApiProperty({ type: [String] })
  excluded: string[];
}

export class DonationRefDataDto {
  @ApiProperty({ type: () => [DonationReferenceOptionDto] })
  donationStatuses?: DonationReferenceOptionDto[];

  @ApiProperty({ type: () => [DonationReferenceOptionDto] })
  donationTypes?: DonationReferenceOptionDto[];

  @ApiProperty({ type: () => [DonationReferenceOptionDto] })
  paymentMethods?: DonationReferenceOptionDto[];

  @ApiProperty({ type: () => [DonationReferenceOptionDto] })
  upiOptions?: DonationReferenceOptionDto[];

  @ApiPropertyOptional({ type: () => DonationStatusGroupsDto })
  donationStatusGroups?: DonationStatusGroupsDto;
}

