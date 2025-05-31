package ngo.nabarun.app.ext.serviceimpl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.auth0.client.auth.AuthAPI;
import com.auth0.client.mgmt.ManagementAPI;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.json.mgmt.connections.ConnectionsPage;
import com.auth0.json.mgmt.emailproviders.EmailProvider;
import com.auth0.json.mgmt.emailproviders.EmailProviderCredentials;
import com.auth0.json.mgmt.resourceserver.ResourceServer;
import com.auth0.json.mgmt.tickets.PasswordChangeTicket;
import com.auth0.json.mgmt.users.User;
import com.auth0.net.Response;
import com.auth0.net.TokenRequest;
import com.auth0.net.client.Auth0HttpClient;
import com.fasterxml.jackson.core.JsonProcessingException;

import ngo.nabarun.app.common.annotation.NoLogging;
import ngo.nabarun.app.common.enums.LoginMethod;
import ngo.nabarun.app.common.helper.PropertyHelper;
import ngo.nabarun.app.common.util.CommonUtils;
import ngo.nabarun.app.ext.config.Auth0OkHttp3Client;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.helpers.ObjectConverter;
import ngo.nabarun.app.ext.helpers.ThirdPartySystem;
import ngo.nabarun.app.ext.objects.AuthAPIInfo;
import ngo.nabarun.app.ext.objects.AuthConnection;
import ngo.nabarun.app.ext.objects.AuthUser;
import ngo.nabarun.app.ext.objects.AuthUserRole;
import ngo.nabarun.app.ext.service.IAuthManagementExtService;


@Service
public class Auth0ManagementExtServiceImpl implements IAuthManagementExtService {

	@Autowired 
	private PropertyHelper propertyHelper;

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

			Auth0HttpClient client = Auth0OkHttp3Client.newBuilder().build();
			managementAPI = ManagementAPI.newBuilder(domain, tokenHolder.getAccessToken()).withHttpClient(client).build();
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
	

	//@Cacheable(value = "auth0_users")
	@Override
	public List<AuthUser> getUsers() throws ThirdPartyException {
		try {
			return initManagementAPI().users().list(null).execute().getBody().getItems().stream()
					.map(m -> ObjectConverter.toAuthUser(m)).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Cacheable(value = "auth0_users",key="#id")
	@Override
	public AuthUser getUser(String id) throws ThirdPartyException {
		try {
			return ObjectConverter.toAuthUser(initManagementAPI().users().get(id, null).execute().getBody());
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@CacheEvict(value = "auth0_users",key="#id")
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
			String password = null;
			boolean userCreated=false;
			for(LoginMethod provider:userDetails.getProviders()) {
				switch(provider) {
				case PASSWORD:
					connection="Username-Password-Authentication";
					password= userDetails.getPassword();
					break;
				case EMAIL:
					connection="email";
					break;
				case SMS:
					connection="sms";
					break;
				}
				Response<User> response=initManagementAPI().users().create(ObjectConverter.toAuth0User(userDetails,connection,password)).execute();
				if(response.getStatusCode() == 201) {
					userCreated=true;
				}
			}	
			if(!userCreated) {
				throw new RuntimeException("User creation failed.");
			}
			/*
			 * create for all connections and merge ids
			 */
			List<User> users= initManagementAPI().users().listByEmail(userDetails.getEmail().toLowerCase(),null).execute().getBody();
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
	
	@Override
	public Void endUserSessions(String userId) throws ThirdPartyException {
		try {
			return initManagementAPI().users().invalidateRememberedBrowsers(userId).execute().getBody();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Cacheable(value = "auth0_user_roles",key="#userId")
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
	
	//@CacheEvict(value = "auth0_user_roles",key="#userId")
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
	
	
	
	@Cacheable(value = "auth0_role_users",key="#roleId")
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
			User updatedUser = ObjectConverter.toAuth0User(auth0User,null,auth0User.getPassword());
			updatedUser = initManagementAPI().users().update(id, updatedUser).execute().getBody();
			return ObjectConverter.toAuthUser(updatedUser);
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Cacheable(value = "auth0_roles")
	@Override
	public List<AuthUserRole> getAllAvailableRoles() throws ThirdPartyException {
		try {
			return initManagementAPI().roles().list(null).execute().getBody().getItems()
					.stream().map(m->ObjectConverter.toAuthUserRole(m)).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}
	
	//@Cacheable(value = "auth0_conn")
	@Override
	public List<AuthConnection> getConnections() throws ThirdPartyException {
		try {
			
			ConnectionsPage connections=initManagementAPI().connections().listAll(null).execute().getBody();
			try {
				System.err.println(CommonUtils.toJSONString(connections, false));
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return connections.getItems().stream().map(m->ObjectConverter.toAuthConnection(m)).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public String loginWithUser(String email, String old_password) throws ThirdPartyException {
		String domain=propertyHelper.getAuth0BaseURL();
		String clientId=propertyHelper.getAuth0ManagementClientId();
		String clientSecret=propertyHelper.getAuth0ManagementClientSecret();
		try {
			AuthAPI authAPI = AuthAPI.newBuilder(domain, clientId,clientSecret).build();
			TokenHolder token=authAPI.login(email, old_password.toCharArray()).execute()
	        .getBody();
			return token.getAccessToken();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@NoLogging
	@Override
	public int updateEmailProvider(boolean enabled,String sender,String apikey_sg) throws ThirdPartyException {
		try {
			EmailProvider ep= new EmailProvider("sendgrid");
			ep.setDefaultFromAddress(sender);
			ep.setEnabled(enabled);
			ep.setCredentials(new EmailProviderCredentials(apikey_sg));
			Response<EmailProvider> response=initManagementAPI().emailProvider().update(ep).execute();
			return response.getStatusCode();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}
	
	@Cacheable(value = "auth0_apis",key="#identifier")
	@Override
	public AuthAPIInfo getAuthAPIInfo(String identifier) throws ThirdPartyException {
		try {
			ResourceServer resourceServer=initManagementAPI().resourceServers().get(identifier).execute().getBody();
			return ObjectConverter.toAuthAPIInfo(resourceServer);
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}
	
	
}
