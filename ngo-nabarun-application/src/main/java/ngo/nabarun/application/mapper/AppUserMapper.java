package ngo.nabarun.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ngo.nabarun.application.dto.command.UserCommand.UserCreateCommand;
import ngo.nabarun.application.dto.command.UserCommand.UserUpdateCommand;
import ngo.nabarun.application.dto.result.UserResult;
import ngo.nabarun.domain.user.model.User;
import ngo.nabarun.domain.user.model.User.UserCreate;
import ngo.nabarun.domain.user.model.User.UserSelfUpdate;
import ngo.nabarun.domain.user.model.User.UserUpdate;

@Mapper(componentModel = "spring")
public interface AppUserMapper {
	
	UserSelfUpdate toUpdateDomain_Self(UserUpdateCommand command);
	UserUpdate toUpdateDomain(UserUpdateCommand command);
	UserUpdate toUpdateDomain(User user);

    @Mapping(source = "phoneCode", target = "number.phoneCode")
    @Mapping(source = "phoneNumber", target = "number.phoneNumber")
	UserCreate toCreateDomain(UserCreateCommand command);
	UserResult toResult(User user);


}
