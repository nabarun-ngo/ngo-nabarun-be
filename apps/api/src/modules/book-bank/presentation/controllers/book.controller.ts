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
  ApiUuidParam,
  PagedResponse,
} from '@nabarun-ngo/nestjs-shared-core';
import { CreateBookCommand } from '../../application/commands/create-book/create-book.command';
import { UpdateBookCommand } from '../../application/commands/update-book/update-book.command';
import { DeleteBookCommand } from '../../application/commands/delete-book/delete-book.command';
import { ApplyBookOperationCommand } from '../../application/commands/apply-book-operation/apply-book-operation.command';
import { ListBooksQuery } from '../../application/queries/list-books/list-books.query';
import { GetBookByIdQuery } from '../../application/queries/get-book-by-id/get-book-by-id.query';
import { GetBookReferenceDataQuery } from '../../application/queries/get-book-reference-data/get-book-reference-data.query';
import { BookMapper } from '../../application/mappers/book.mapper';
import {
  ApplyBookOperationDto,
  BookDetailDto,
  BookDetailFilterDto,
  BookReferenceDataDto,
  CreateBookDto,
  UpdateBookDto,
} from '../../application/dtos/book.dto';

@ApiTags('Book Bank')
@ApiBearerAuth('jwt')
@ApiSecurity('api-key')
@UseGuards(UnifiedAuthGuard)
@Controller('books')
export class BookController {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) {}

  @Post('create')
  @HttpCode(HttpStatus.CREATED)
  @RequirePermissions('create:book')
  @ApiAutoResponse(BookDetailDto, { status: HttpStatus.CREATED })
  async createBook(@Body() dto: CreateBookDto, @CurrentUser() user: AuthUser): Promise<BookDetailDto> {
    const book = await this.commandBus.execute(
      new CreateBookCommand({ ...dto, createdById: requireUserId(user) }),
    );
    return BookMapper.toDto(book);
  }

  @Get('list')
  @RequirePermissions('read:books')
  @ApiAutoPagedResponse(BookDetailDto)
  listBooks(
    @Query() filter?: BookDetailFilterDto,
  ): Promise<PagedResponse<BookDetailDto>> {
    return this.queryBus.execute(new ListBooksQuery(filter));
  }

  @Get('static/referenceData')
  @RequirePermissions('read:books')
  @ApiAutoResponse(BookReferenceDataDto)
  getReferenceData(): Promise<BookReferenceDataDto> {
    return this.queryBus.execute(new GetBookReferenceDataQuery());
  }

  @Get(':id')
  @RequirePermissions('read:books')
  @ApiUuidParam('id', 'Identifier of the book')
  @ApiAutoResponse(BookDetailDto)
  getBookById(@Param('id') id: string): Promise<BookDetailDto> {
    return this.queryBus.execute(new GetBookByIdQuery(id));
  }

  @Put('update/:id')
  @RequirePermissions('update:book')
  @ApiUuidParam('id', 'Identifier of the book')
  @ApiAutoResponse(BookDetailDto)
  async updateBook(
    @Param('id') id: string,
    @Body() dto: UpdateBookDto,
    @CurrentUser() user: AuthUser,
  ): Promise<BookDetailDto> {
    const book = await this.commandBus.execute(
      new UpdateBookCommand({ id, ...dto, updatedById: requireUserId(user) }),
    );
    return BookMapper.toDto(book);
  }

  @Post(':id/operations')
  @RequirePermissions('update:book')
  @ApiUuidParam('id', 'Identifier of the book')
  @ApiAutoResponse(BookDetailDto)
  async applyOperation(
    @Param('id') id: string,
    @Body() dto: ApplyBookOperationDto,
    @CurrentUser() user: AuthUser,
  ): Promise<BookDetailDto> {
    const book = await this.commandBus.execute(
      new ApplyBookOperationCommand({
        id,
        ...dto,
        actedById: requireUserId(user),
      }),
    );
    return BookMapper.toDto(book);
  }

  @Delete(':id')
  @RequirePermissions('delete:book')
  @ApiAutoVoidResponse({ status: HttpStatus.NO_CONTENT, description: 'Book deleted' })
  @ApiUuidParam('id', 'Identifier of the book')
  @HttpCode(HttpStatus.NO_CONTENT)
  deleteBook(@Param('id') id: string): Promise<void> {
    return this.commandBus.execute(new DeleteBookCommand(id));
  }
}
