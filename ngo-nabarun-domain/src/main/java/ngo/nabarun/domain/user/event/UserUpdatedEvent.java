package ngo.nabarun.domain.user.event;

import ngo.nabarun.common.event.CustomEvent;
import ngo.nabarun.domain.user.vo.AdminUpdate;
import ngo.nabarun.domain.user.vo.ProfileUpdate;
import ngo.nabarun.domain.user.vo.RoleUpdateResult;

public record UserUpdatedEvent(String id, String userId,ProfileUpdate profileChanges, AdminUpdate adminChanges,RoleUpdateResult role) implements CustomEvent {

}
