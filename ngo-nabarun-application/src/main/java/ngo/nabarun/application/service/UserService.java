package ngo.nabarun.application.service;

import java.util.List;
import java.util.Optional;

import ngo.nabarun.application.dto.command.UserCommand.UserCreateCommand;
import ngo.nabarun.application.dto.command.UserCommand.UserSelfUpdateCommand;
import ngo.nabarun.application.dto.command.UserCommand.UserUpdateCommand;
import ngo.nabarun.application.dto.result.UserResult;
import ngo.nabarun.domain.user.enums.UserStatus;

public interface UserService {

    UserResult createUser(UserCreateCommand command);

    // Queries
    PagedResult<UserResult> listUsers(String emailLike, UserStatus status, String roleCode, int page, int size, String sort);
    Optional<UserResult> getUser(String id);

    // Commands
    UserResult updateMyDetails(String id, UserSelfUpdateCommand command);
    UserResult updateUser(String id, UserUpdateCommand command);

    record PagedResult<T>(List<T> items, long total, int page, int size) {}
}
