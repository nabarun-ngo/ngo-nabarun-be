package ngo.nabarun.domain.user.event;

import ngo.nabarun.domain.user.model.User;

public record UserCreatedEvent(User user,String source) {
	
}


