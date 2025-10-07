package ngo.nabarun.application.service;

import ngo.nabarun.application.dto.command.UserCommand.UserCreateCommand;
import ngo.nabarun.application.dto.result.UserResult;

public interface UserService {

	UserResult createUser(UserCreateCommand command);

}
