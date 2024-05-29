package ngo.nabarun.app.ext.serviceimpl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
import ngo.nabarun.app.ext.objects.AuthConnection;
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

	@Cacheable("all_auth0_users")
	@Override
	public List<AuthUser> getUsers() throws ThirdPartyException {
		try {
			return initManagementAPI().users().list(null).execute().getBody().getItems().stream()
					.map(m -> ObjectConverter.toAuthUser(m)).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Cacheable("Auth0User+#id")
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

	/**
	 * this will always return the first user
	 */
	@Override
	public AuthUser createUser(AuthUser userDetails) throws ThirdPartyException {
		
		try {
			String connection = null;
			for(String provider:userDetails.getProviders()) {
				switch(provider) {
				case "PASSWORD":
					connection="Username-Password-Authentication";
					break;
				case "EMAIL":
					connection="email";
					userDetails.setPassword(null);
					break;
				case "SMS":
					connection="sms";
					userDetails.setPassword(null);
					break;
				}
				initManagementAPI().users().create(ObjectConverter.toAuth0User(userDetails,connection)).execute();
			}		
			/*
			 * create for all connections and merge ids
			 */
			List<User> users= initManagementAPI().users().listByEmail(userDetails.getEmail(), null).execute().getBody();
			if(users.size() >1) {
				Map<String, Object> baseMetadata = users.get(0).getUserMetadata();
				String primaryUserId=users.get(0).getId();
				for(int i=1;i<users.size();i++) {
					String secondaryUserId=users.get(i).getId();
					String provider=users.get(i).getIdentities().get(0).getProvider();
					baseMetadata.putAll(users.get(i).getUserMetadata());
					initManagementAPI().users().linkIdentity(primaryUserId, secondaryUserId, provider, null).execute();
				}
				/*
				 * Updating the user metadata
				 */
				User updatedUser= new User();
				updatedUser.setUserMetadata(baseMetadata);
				updatedUser = initManagementAPI().users().update(primaryUserId, updatedUser).execute().getBody();
				return ObjectConverter.toAuthUser(updatedUser);
			}
			return ObjectConverter.toAuthUser(users.get(0));
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Cacheable("Auth0Role+#userId")
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
	
	@Cacheable("Auth0Users+#roleId")
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
			User updatedUser = ObjectConverter.toAuth0User(auth0User,null);
			updatedUser = initManagementAPI().users().update(id, updatedUser).execute().getBody();
			return ObjectConverter.toAuthUser(updatedUser);
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Cacheable("Auth0AllRoles")
	@Override
	public List<AuthUserRole> getAllAvailableRoles() throws ThirdPartyException {
		try {
			return initManagementAPI().roles().list(null).execute().getBody().getItems()
					.stream().map(m->ObjectConverter.toAuthUserRole(m)).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}
	
	@Cacheable("Auth0AllConn")
	@Override
	public List<AuthConnection> getConnections() throws ThirdPartyException {
		try {
			return initManagementAPI().connections().listAll(null).execute().getBody().getItems().stream().map(m->ObjectConverter.toAuthConnection(m)).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}
	
	
}
