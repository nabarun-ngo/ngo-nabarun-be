package ngo.nabarun.domain.user.event;

import ngo.nabarun.domain.user.vo.AdminUpdate;
import ngo.nabarun.domain.user.vo.ProfileUpdate;
import ngo.nabarun.domain.user.vo.RoleUpdateResult;
import ngo.nabarun.event.AppEvent;

public record UserUpdatedEvent(String id, String userId,ProfileUpdate profileChanges, AdminUpdate adminChanges,RoleUpdateResult role) implements AppEvent {

}
