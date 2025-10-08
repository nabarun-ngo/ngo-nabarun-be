package ngo.nabarun.domain.user.event;

import ngo.nabarun.common.event.CustomEvent;

public record UserCreatedEvent(String id,String userId) implements CustomEvent {
	
}


