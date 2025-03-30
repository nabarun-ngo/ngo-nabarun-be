package ngo.nabarun.app.ext.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.objects.AuthAPIInfo;
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
	
	List<AuthUserRole> getAllAvailableRoles() throws ThirdPartyException;
	List<AuthConnection> getConnections() throws ThirdPartyException;

	String loginWithUser(String email, String old_password) throws ThirdPartyException;

	Void endUserSessions(String userId) throws ThirdPartyException;

	int updateEmailProvider(boolean enabled,String sender,String apikey_sg) throws ThirdPartyException;

	AuthAPIInfo getAuthAPIInfo(String identifier) throws ThirdPartyException;
}
