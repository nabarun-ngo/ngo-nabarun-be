import { Injectable } from '@nestjs/common';
import { CommandBus, QueryBus } from '@nestjs/cqrs';
import { SortOrder } from '@nabarun-ngo/nestjs-shared-core';
import { UserAdminUpdateProps } from '../../domain/aggregates/user/user.aggregate';
import { UserFilter } from '../../domain/repositories/user.repository';
import { CreateUserCommand } from '../commands/create-user/create-user.command';
import { DeleteUserCommand } from '../commands/delete-user/delete-user.command';
import { UpdateUserAdminCommand } from '../commands/update-user-admin/update-user-admin.command';
import { UserListResponseDto, UserResponseDto } from '../dtos/user-response.dto';
import { GetUserByEmailQuery } from '../queries/get-user-by-email/get-user-by-email.query';
import { ListUsersQuery } from '../queries/list-users/list-users.query';

export type UserFacadePagination = {
  pageIndex?: number;
  pageSize?: number;
};

export type UserFacadeListOptions = UserFacadePagination & {
  sortBy?: string;
  sortDir?: SortOrder;
};

@Injectable()
export class UserFacade {
  constructor(
    private readonly commandBus: CommandBus,
    private readonly queryBus: QueryBus,
  ) {}

  findUserByEmail(email: string): Promise<UserResponseDto | null> {
    return this.queryBus.execute(new GetUserByEmailQuery(email));
  }

  listUsers(
    filter: UserFilter = {},
    options?: UserFacadeListOptions,
  ): Promise<UserListResponseDto> {
    return this.queryBus.execute(
      new ListUsersQuery(
        filter,
        options?.pageIndex ?? 0,
        options?.pageSize ?? 20,
        options?.sortBy,
        options?.sortDir,
      ),
    );
  }

  createUser(params: CreateUserCommand['params']): Promise<UserResponseDto> {
    return this.commandBus.execute(new CreateUserCommand(params));
  }

  updateUserAdmin(params: {
    userId: string;
    adminId: string;
    detail: UserAdminUpdateProps;
  }): Promise<UserResponseDto> {
    return this.commandBus.execute(new UpdateUserAdminCommand(params));
  }

  deleteUser(params: { userId: string; adminId: string }): Promise<UserResponseDto> {
    return this.commandBus.execute(new DeleteUserCommand(params));
  }
}
