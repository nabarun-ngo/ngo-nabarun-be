package ngo.nabarun.application.listener;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ngo.nabarun.application.AdditionalFieldKey;
import ngo.nabarun.application.dto.command.UserCommand.UserCreateCommand;
import ngo.nabarun.application.service.UserService;
import ngo.nabarun.domain.request.enums.RequestType;
import ngo.nabarun.domain.request.event.RequestWorkflowFulfilledEvent;
import ngo.nabarun.event.handler.AppEventHandler;

public class RequestWorkflowFulfilledEventHandler implements AppEventHandler<RequestWorkflowFulfilledEvent> {

    @Autowired private UserService userRepo;

	@Override
	public void handle(RequestWorkflowFulfilledEvent ev) throws Exception {
		if(ev.type() == RequestType.JOIN_REQUEST || ev.type() == RequestType.JOIN_REQUEST_USER) {
    		Map<String,String> data = ev.data();
    		UserCreateCommand command= new UserCreateCommand();
        	command.setFirstName(data.get(AdditionalFieldKey.firstName.name()));
        	command.setLastName(data.get(AdditionalFieldKey.lastName.name()));
        	command.setEmail(data.get(AdditionalFieldKey.email.name()));
        	command.setPhoneCode(data.get(AdditionalFieldKey.dialCode.name()));
        	command.setPhoneNumber(data.get(AdditionalFieldKey.mobileNumber.name()));
        	command.setEmailverified(ev.type() == RequestType.JOIN_REQUEST);
        	userRepo.createUser(command);
    	}
	}

}
