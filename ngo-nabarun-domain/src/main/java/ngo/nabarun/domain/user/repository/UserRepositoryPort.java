package ngo.nabarun.domain.user.repository;

import java.util.List;

import ngo.nabarun.domain.user.model.Role;
import ngo.nabarun.domain.user.model.User;

public interface UserRepositoryPort {
	
	User createUser(User user);
	User updateUser(User user);
	boolean isUserExists(String email);
	User assignRolesToUser(User user, List<Role> rolesToAdd);
	User deleteRolesFromUser(User user, List<Role> rolesToDelete);

}
