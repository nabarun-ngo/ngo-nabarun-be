package ngo.nabarun.application.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import ngo.nabarun.application.event.IAMUserCreated;
import ngo.nabarun.application.port.IAMPort;
import ngo.nabarun.application.service.UserService;
import ngo.nabarun.domain.user.event.UserCreatedEvent;
import ngo.nabarun.domain.user.model.User;
import ngo.nabarun.domain.user.repository.UserRepositoryPort;

@Component
public class UserEventListener {

	@Autowired
	private UserRepositoryPort userRepoPort;
	
	@Autowired private IAMPort iamPort;
	
	@Autowired private ApplicationEventPublisher eventPublisher;

	@EventListener
	public void handle(IAMUserCreated event) {
		event.user();
		//userRepoPort.updateUser();
	}

	@EventListener
	public void handle(UserCreatedEvent event) {
		User user = iamPort.createUser(event.user());
		eventPublisher.publishEvent(new IAMUserCreated(user));
	}

}
