import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import {
  IsDateString,
  IsEnum,
  IsNotEmpty,
  IsOptional,
  IsString,
  ValidateIf,
} from 'class-validator';
import {
  BookAcquisitionType,
  BookCategory,
  BookClassLevel,
  BookOperation,
  BookStatus,
  BookSubject,
} from '../../domain/enums/book.enum';
import { PaginatedQueryDto } from '@nabarun-ngo/nestjs-shared-core';

export class BookLoanRecordDto {
  @ApiProperty() id!: string;
  @ApiPropertyOptional() borrowerUserId?: string;
  @ApiPropertyOptional() guestName?: string;
  @ApiProperty() loanedAt!: Date;
  @ApiPropertyOptional() dueDate?: Date;
  @ApiPropertyOptional() returnedAt?: Date;
  @ApiPropertyOptional() returnedById?: string;
  @ApiPropertyOptional() notes?: string;
}

export class CreateBookDto {
  @ApiProperty({ example: 'NCERT Mathematics Class 8' })
  @IsNotEmpty()
  @IsString()
  title!: string;

  @ApiProperty({ example: 'NCERT' })
  @IsNotEmpty()
  @IsString()
  author!: string;

  @ApiProperty({ enum: BookCategory })
  @IsNotEmpty()
  @IsEnum(BookCategory)
  category!: BookCategory;

  @ApiProperty({ enum: BookSubject })
  @IsNotEmpty()
  @IsEnum(BookSubject)
  subject!: BookSubject;

  @ApiProperty({ enum: BookClassLevel })
  @IsNotEmpty()
  @IsEnum(BookClassLevel)
  classLevel!: BookClassLevel;

  @ApiProperty({ enum: BookAcquisitionType })
  @IsNotEmpty()
  @IsEnum(BookAcquisitionType)
  acquisitionType!: BookAcquisitionType;

  @ApiPropertyOptional({ example: '978-81-7450-123-4' })
  @IsOptional()
  @IsString()
  isbn?: string;

  @ApiPropertyOptional({ example: 'Ramesh house — 2nd floor shelf' })
  @IsOptional()
  @IsString()
  location?: string;

  @ApiPropertyOptional({ example: 'Donated by local school' })
  @IsOptional()
  @IsString()
  acquisitionNotes?: string;
}

export class UpdateBookDto {
  @ApiPropertyOptional() @IsOptional() @IsString() title?: string;
  @ApiPropertyOptional() @IsOptional() @IsString() author?: string;
  @ApiPropertyOptional({ enum: BookCategory }) @IsOptional() @IsEnum(BookCategory) category?: BookCategory;
  @ApiPropertyOptional({ enum: BookSubject }) @IsOptional() @IsEnum(BookSubject) subject?: BookSubject;
  @ApiPropertyOptional({ enum: BookClassLevel }) @IsOptional() @IsEnum(BookClassLevel) classLevel?: BookClassLevel;
  @ApiPropertyOptional({ enum: BookAcquisitionType })
  @IsOptional()
  @IsEnum(BookAcquisitionType)
  acquisitionType?: BookAcquisitionType;
  @ApiPropertyOptional() @IsOptional() @IsString() isbn?: string;
  @ApiPropertyOptional() @IsOptional() @IsString() location?: string;
  @ApiPropertyOptional() @IsOptional() @IsString() acquisitionNotes?: string;
}

export class ApplyBookOperationDto {
  @ApiProperty({ enum: BookOperation })
  @IsNotEmpty()
  @IsEnum(BookOperation)
  operation!: BookOperation;

  @ApiPropertyOptional({ example: 'b41d7e60-9c38-4a15-8f27-6d0e2a9b3c41' })
  @ValidateIf((o: ApplyBookOperationDto) => !!o.borrowerUserId)
  @IsString()
  borrowerUserId?: string;

  @ApiPropertyOptional({ example: 'Guest Student — Ananya' })
  @ValidateIf((o: ApplyBookOperationDto) => !!o.guestName)
  @IsString()
  guestName?: string;

  @ApiPropertyOptional({ example: '2026-09-30' })
  @IsOptional()
  @IsDateString()
  dueDate?: string;

  @ApiPropertyOptional()
  @IsOptional()
  @IsString()
  notes?: string;

  @ApiPropertyOptional({ example: 'School office cupboard' })
  @ValidateIf((o: ApplyBookOperationDto) => o.operation === BookOperation.TRANSFER_LOCATION)
  @IsNotEmpty()
  @IsString()
  location?: string;
}

export class BookDetailDto {
  @ApiProperty() id!: string;
  @ApiProperty() title!: string;
  @ApiProperty() author!: string;
  @ApiProperty({ enum: BookCategory }) category!: BookCategory;
  @ApiProperty({ enum: BookSubject }) subject!: BookSubject;
  @ApiProperty({ enum: BookClassLevel }) classLevel!: BookClassLevel;
  @ApiPropertyOptional() isbn?: string;
  @ApiPropertyOptional() location?: string;
  @ApiProperty({ enum: BookStatus }) status!: BookStatus;
  @ApiProperty({ enum: BookAcquisitionType }) acquisitionType!: BookAcquisitionType;
  @ApiPropertyOptional() acquisitionNotes?: string;
  @ApiPropertyOptional() holderUserId?: string;
  @ApiPropertyOptional() holderGuestName?: string;
  @ApiPropertyOptional() createdById?: string;
  @ApiPropertyOptional() updatedById?: string;
  @ApiPropertyOptional({ type: [BookLoanRecordDto] }) loanHistory?: BookLoanRecordDto[];
  @ApiProperty() createdAt!: Date;
  @ApiProperty() updatedAt!: Date;
}

export class BookDetailFilterDto extends PaginatedQueryDto {
  @ApiPropertyOptional({ enum: BookStatus }) @IsOptional() @IsEnum(BookStatus) status?: BookStatus;
  @ApiPropertyOptional({ enum: BookCategory }) @IsOptional() @IsEnum(BookCategory) category?: BookCategory;
  @ApiPropertyOptional() @IsOptional() @IsString() author?: string;
  @ApiPropertyOptional({ enum: BookSubject }) @IsOptional() @IsEnum(BookSubject) subject?: BookSubject;
  @ApiPropertyOptional({ enum: BookClassLevel }) @IsOptional() @IsEnum(BookClassLevel) classLevel?: BookClassLevel;
  @ApiPropertyOptional() @IsOptional() @IsString() location?: string;
  @ApiPropertyOptional() @IsOptional() @IsString() holderUserId?: string;
  @ApiPropertyOptional({ enum: BookAcquisitionType })
  @IsOptional()
  @IsEnum(BookAcquisitionType)
  acquisitionType?: BookAcquisitionType;
  @ApiPropertyOptional({ description: 'Search title, author, isbn, location' })
  @IsOptional()
  @IsString()
  q?: string;
}

export class BookReferenceDataDto {
  @ApiProperty({ type: [String], enum: BookStatus }) statuses!: string[];
  @ApiProperty({ type: [String], enum: BookCategory }) categories!: string[];
  @ApiProperty({ type: [String], enum: BookSubject }) subjects!: string[];
  @ApiProperty({ type: [String], enum: BookClassLevel }) classLevels!: string[];
  @ApiProperty({ type: [String], enum: BookAcquisitionType }) acquisitionTypes!: string[];
  @ApiProperty({ type: [String], enum: BookOperation }) operations!: string[];
}
