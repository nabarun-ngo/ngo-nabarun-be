package ngo.nabarun.domain.user.event;

import ngo.nabarun.event.AppEvent;

public record UserCreatedEvent(String id,String userId) implements AppEvent {
	
}


