package ngo.nabarun.application.listener;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ngo.nabarun.application.port.IAMPort;
import ngo.nabarun.domain.user.event.UserUpdatedEvent;
import ngo.nabarun.event.handler.AppEventHandler;

@Component
public class UserUpdatedEventHandler implements AppEventHandler<UserUpdatedEvent>{
	
	@Autowired private IAMPort iamPort;

	@Override
	public void handle(UserUpdatedEvent event) throws Exception {
		System.out.println(event);

		if(event.adminChanges() != null) {
			
		}
		
		if(event.role() != null) {
			var roleIdMap =iamPort.getAllAvailableRoles().stream()
            .collect(Collectors.toMap(e->e.roleCode(), e->e.roleId()));
			System.out.println(roleIdMap);
			var toAdd=event.role().rolesToAdd().stream().map(m-> roleIdMap.get(m.roleCode())).toList();
			var toRemove=event.role().rolesToRemove().stream().map(m-> roleIdMap.get(m.roleCode())).toList();
			System.out.println(toAdd);
			System.out.println(toRemove);

		}
		
		if(event.profileChanges() != null) {
			
		}
		
		
		
		
	}

}
