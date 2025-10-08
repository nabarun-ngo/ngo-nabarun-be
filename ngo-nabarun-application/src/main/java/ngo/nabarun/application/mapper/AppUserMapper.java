package ngo.nabarun.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ngo.nabarun.application.dto.command.UserCommand.UserCreateCommand;
import ngo.nabarun.application.dto.command.UserCommand.UserSelfUpdateCommand;
import ngo.nabarun.application.dto.command.UserCommand.UserUpdateCommand;
import ngo.nabarun.application.dto.result.UserResult;
import ngo.nabarun.domain.user.model.User;
import ngo.nabarun.domain.user.vo.AdminUpdate;
import ngo.nabarun.domain.user.vo.ProfileUpdate;
import ngo.nabarun.domain.user.vo.RegistrationDetail;

@Mapper(componentModel = "spring")
public interface AppUserMapper {
	
	ProfileUpdate toProfileUpdate(UserSelfUpdateCommand command);
	AdminUpdate toAdminUpdate(UserUpdateCommand command);

    @Mapping(source = "phoneCode", target = "number.phoneCode")
    @Mapping(source = "phoneNumber", target = "number.phoneNumber")
	RegistrationDetail toRegistrationDetail(UserCreateCommand command);
	UserResult toUserResult(User user);


}
