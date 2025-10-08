package ngo.nabarun.application.port;

import java.util.List;

import ngo.nabarun.domain.user.model.Role;
import ngo.nabarun.domain.user.model.User;


public interface IAMPort {

	List<User> getUserByEmail(String email);
	List<User> getUsers();
	User getUser(String id);
	void deleteUser(String id);
	/**
	 * this will always return the first user
	 * @param isEmailVerified 
	 */
	User createUser(User user, Boolean isEmailVerified);
	List<Role> getAllAvailableRoles();
	String loginWithUser(String email, String password);
	List<String> getScopes(String identifier);
	void assignRolesToUser(String userId, List<String> roleIds);
	void removeRolesFromUser(String userId, List<String> roleIds);
	void assignUsersToRole(String roleId, List<String> userIds);
	User updateUser(String id, User user);
}
