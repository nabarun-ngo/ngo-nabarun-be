package ngo.nabarun.app.ext.serviceimpl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.tickets.PasswordChangeTicket;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.TokenRequest;

import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.helpers.ObjectConverter;
import ngo.nabarun.app.ext.helpers.ThirdPartySystem;
import ngo.nabarun.app.ext.objects.AuthUser;
import ngo.nabarun.app.ext.objects.AuthUserRole;
import ngo.nabarun.app.ext.service.IAuthManagementExtService;

@Service
public class Auth0ManagementExtServiceImpl implements IAuthManagementExtService {

	@Autowired 
	private GenericPropertyHelper propertyHelper;

	private static ManagementAPI managementAPI;
	private static TokenHolder tokenHolder;

	private ManagementAPI initManagementAPI() throws Auth0Exception {
		if (tokenHolder == null || (tokenHolder != null && new Date().after(tokenHolder.getExpiresAt()))) {
			String domain=propertyHelper.getAuth0BaseURL();
			String clientId=propertyHelper.getAuth0ManagementClientId();
			String clientSecret=propertyHelper.getAuth0ManagementClientSecret();
			AuthAPI authAPI = AuthAPI.newBuilder(domain, clientId, clientSecret).build();
			String managementAudience=propertyHelper.getAuth0ManagementAPIAudience();
			TokenRequest tokenRequest = authAPI.requestToken(managementAudience);
			tokenHolder = tokenRequest.execute().getBody();
			managementAPI = ManagementAPI.newBuilder(domain, tokenHolder.getAccessToken()).build();
		}
		return managementAPI;
	}

	@Override
	public List<AuthUser> getUserByEmail(String email) throws ThirdPartyException {
		try {
			return initManagementAPI().users().listByEmail(email, null).execute().getBody().stream()
					.map(m -> ObjectConverter.toAuthUser(m)).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public List<AuthUser> getUsers() throws ThirdPartyException {
		try {
			return initManagementAPI().users().list(null).execute().getBody().getItems().stream()
					.map(m -> ObjectConverter.toAuthUser(m)).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public AuthUser getUser(String id) throws ThirdPartyException {
		try {
			return ObjectConverter.toAuthUser(initManagementAPI().users().get(id, null).execute().getBody());
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public void deleteUser(String id) throws ThirdPartyException {
		try {
			initManagementAPI().users().delete(id).execute().getBody();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public AuthUser createUser(AuthUser userDetails) throws ThirdPartyException {
		try {
			return ObjectConverter.toAuthUser(
					initManagementAPI().users().create(ObjectConverter.toAuth0User(userDetails)).execute().getBody());
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public List<AuthUserRole> getRoles(String userId) throws ThirdPartyException {
		try {
			return initManagementAPI().users().listRoles(userId, null).execute().getBody().getItems()
					.stream().map(m->ObjectConverter.toAuthUserRole(m)).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public void assignRolesToUser(String userId, List<String> roleIds) throws ThirdPartyException {
		try {
			System.out.println(roleIds);
			initManagementAPI().users().addRoles(userId, roleIds).execute().getBody();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}
	
	@Override
	public void removeRolesFromUser(String userId, List<String> roleIds) throws ThirdPartyException {
		try {
			initManagementAPI().users().removeRoles(userId, roleIds).execute().getBody();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}
	
	@Override
	public void assignUsersToRole(String roleId, List<String> userIds) throws ThirdPartyException {
		try {
			initManagementAPI().roles().assignUsers(roleId,  userIds).execute().getBody();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}
	
	@Override
	public List<AuthUser> listUsersByRole(String roleId) throws ThirdPartyException {
		try {
			return initManagementAPI().roles().listUsers(roleId, null).execute().getBody().getItems().stream()
			.map(m -> ObjectConverter.toAuthUser(m)).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public String createPasswordResetTicket(String userId, String clientId, int ttlInSec) throws ThirdPartyException {
		PasswordChangeTicket ticketReq = new PasswordChangeTicket(userId);
		try {
			ticketReq.setClientId(clientId);
			ticketReq.setTTLSeconds(ttlInSec);
			PasswordChangeTicket ticketResp = initManagementAPI().tickets().requestPasswordChange(ticketReq).execute()
					.getBody();
			return ticketResp.getTicket();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public AuthUser updateUser(String id, AuthUser auth0User) throws ThirdPartyException {
		try {
			User updatedUser = ObjectConverter.toAuth0User(auth0User);
			updatedUser = initManagementAPI().users().update(id, updatedUser).execute().getBody();
			return ObjectConverter.toAuthUser(updatedUser);
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public List<AuthUserRole> getAllAvailableRoles() throws ThirdPartyException {
		try {
			return initManagementAPI().roles().list(null).execute().getBody().getItems()
					.stream().map(m->ObjectConverter.toAuthUserRole(m)).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

}
