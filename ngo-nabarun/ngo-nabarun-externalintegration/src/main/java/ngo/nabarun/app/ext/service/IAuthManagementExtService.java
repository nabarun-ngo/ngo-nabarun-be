package ngo.nabarun.app.ext.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.objects.AuthConnection;
import ngo.nabarun.app.ext.objects.AuthUser;
import ngo.nabarun.app.ext.objects.AuthUserRole;


@Service
public interface IAuthManagementExtService {
	
	List<AuthUser> getUserByEmail(String email) throws ThirdPartyException;
    List<AuthUser> getUsers() throws ThirdPartyException;
    AuthUser getUser(String id) throws ThirdPartyException;
	void deleteUser(String id) throws ThirdPartyException;
	AuthUser createUser(AuthUser userDetails) throws ThirdPartyException;  
	String createPasswordResetTicket(String userId,String clientId,int ttlInSec) throws ThirdPartyException;

	List<AuthUserRole> getRoles(String userId) throws ThirdPartyException;
	AuthUser updateUser(String id, AuthUser auth0User) throws ThirdPartyException;
	void assignUsersToRole(String roleId, List<String> userIds) throws ThirdPartyException;
	void assignRolesToUser(String userId,List<String> roleIds) throws ThirdPartyException;
	void removeRolesFromUser(String userId, List<String> roleIds) throws ThirdPartyException;
	List<AuthUser> listUsersByRole(String roleId) throws ThirdPartyException;
	
	@Cacheable("all_available_roles")
	List<AuthUserRole> getAllAvailableRoles() throws ThirdPartyException;
	@Cacheable("all_connections")
	List<AuthConnection> getConnections() throws ThirdPartyException;
}
