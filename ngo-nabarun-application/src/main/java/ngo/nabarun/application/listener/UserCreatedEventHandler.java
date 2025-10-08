package ngo.nabarun.application.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ngo.nabarun.application.dto.command.UserCommand.UserUpdateCommand;
import ngo.nabarun.application.service.UserService;
import ngo.nabarun.domain.user.enums.UserStatus;
import ngo.nabarun.domain.user.event.UserCreatedEvent;
import ngo.nabarun.domain.user.port.RoleMetadataPort;
import ngo.nabarun.event.handler.AppEventHandler;

@Component
public class UserCreatedEventHandler implements AppEventHandler<UserCreatedEvent> {

	@Autowired
	private RoleMetadataPort roleMetadataPort;

	@Autowired
	private UserService userService;
	
    @Override
    public void handle(UserCreatedEvent event) {
		UserUpdateCommand command = new UserUpdateCommand();
		command.setRoleCodes(roleMetadataPort.getDefaultRoles().stream().map(m->m.roleCode()).toList());
		command.setStatus(UserStatus.ACTIVE);
		command.setUserId(event.userId());
		userService.updateUser(event.id(), command);
	}
}
