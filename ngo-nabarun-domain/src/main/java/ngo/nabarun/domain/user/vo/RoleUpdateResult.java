package ngo.nabarun.domain.user.vo;

import java.util.Collections;
import java.util.List;
import ngo.nabarun.domain.user.model.Role;

public record RoleUpdateResult(List<Role> rolesToAdd, List<Role> rolesToRemove) {

	public static RoleUpdateResult empty() {
		return new RoleUpdateResult(Collections.emptyList(), Collections.emptyList());
	}

	public boolean hasChanges() {
		return !rolesToAdd.isEmpty() || !rolesToRemove.isEmpty();
	}
}
