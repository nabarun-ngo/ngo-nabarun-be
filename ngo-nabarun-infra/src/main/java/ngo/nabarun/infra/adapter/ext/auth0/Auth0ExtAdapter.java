package ngo.nabarun.infra.adapter.ext.auth0;

import java.util.Date;
import java.util.HashMap;
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
import com.auth0.json.mgmt.resourceserver.ResourceServer;
import com.auth0.net.Response;
import com.auth0.net.TokenRequest;
import ngo.nabarun.application.ConfigKey;
import ngo.nabarun.application.port.IAMPort;
import ngo.nabarun.common.props.PropertyHelper;
import ngo.nabarun.domain.user.enums.LoginMethod;
import ngo.nabarun.domain.user.model.Role;
import ngo.nabarun.domain.user.model.User;
import ngo.nabarun.infra.exception.ThirdPartyException;
import ngo.nabarun.infra.exception.ThirdPartyException.ThirdPartySystem;
import ngo.nabarun.infra.mapper.InfraUserMapper;

@Service
public class Auth0ExtAdapter implements IAMPort {

	@Autowired
	private PropertyHelper propertyHelper;

	@Autowired
	private InfraUserMapper userMapper;

	private static ManagementAPI managementAPI;
	private static TokenHolder tokenHolder;

	private ManagementAPI initManagementAPI() throws Auth0Exception {
		if (tokenHolder == null || (tokenHolder != null && new Date().after(tokenHolder.getExpiresAt()))) {
			String domain = propertyHelper.get(ConfigKey.AUTH0_BASE_URL);
			String clientId = propertyHelper.get(ConfigKey.AUTH0_MANAGEMENT_CLIENT_ID);
			String clientSecret = propertyHelper.get(ConfigKey.AUTH0_MANAGEMENT_CLIENT_SECRET);
			AuthAPI authAPI = AuthAPI.newBuilder(domain, clientId, clientSecret).build();
			String managementAudience = propertyHelper.get(ConfigKey.AUTH0_MANAGEMENT_API_AUDIENCE);
			TokenRequest tokenRequest = authAPI.requestToken(managementAudience);
			tokenHolder = tokenRequest.execute().getBody();
			managementAPI = ManagementAPI.newBuilder(domain, tokenHolder.getAccessToken()).build();
		}
		return managementAPI;
	}

	@Override
	public List<User> getUserByEmail(String email) {
		try {
			var users = initManagementAPI().users().listByEmail(email, null).execute().getBody();
			return users.stream().map(userMapper::toDomain).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public List<User> getUsers() {
		try {
			return initManagementAPI().users().list(null).execute().getBody().getItems().stream()
					.map(userMapper::toDomain).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Cacheable(value = "auth0_users", key = "#id")
	@Override
	public User getUser(String id) {
		try {
			return userMapper.toDomain(initManagementAPI().users().get(id, null).execute().getBody());
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@CacheEvict(value = "auth0_users", key = "#id")
	@Override
	public void deleteUser(String id) {
		try {
			initManagementAPI().users().delete(id).execute().getBody();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	/**
	 * this will always return the first user
	 * 
	 * @param isEmailVerified
	 */
	@Override
	public User createUser(User user, Boolean isEmailVerified) {

		try {
			com.auth0.json.mgmt.users.User auth0User=null;
			String connection = null;
			String password = null;
			boolean userCreated = false;
			for (LoginMethod provider : user.getLoginMethod()) {
				switch (provider) {
				case PASSWORD:
					connection = "Username-Password-Authentication";
					password = user.getPassword();
					break;
				case EMAIL:
					connection = "email";
					break;
				case SMS:
					connection = "sms";
					break;
				}
				auth0User = toAuth0User(user, connection,password, isEmailVerified, true);
				Response<com.auth0.json.mgmt.users.User> response = initManagementAPI().users().create(auth0User)
						.execute();
				if (response.getStatusCode() == 201) {
					userCreated = true;
					auth0User = response.getBody();
				}
			}
			if (!userCreated) {
				throw new ThirdPartyException("User creation failed.", ThirdPartySystem.AUTH0);
			}
			/*
			 * create for all connections and merge ids
			 */
			List<com.auth0.json.mgmt.users.User> users = initManagementAPI().users()
					.listByEmail(auth0User.getEmail().toLowerCase(), null).execute().getBody();
			if (users.size() > 1) {
				Map<String, Object> baseMetadata = users.get(0).getUserMetadata();
				String primaryUserId = users.get(0).getId();
				for (int i = 1; i < users.size(); i++) {
					String secondaryUserId = users.get(i).getId();
					String provider = users.get(i).getIdentities().get(0).getProvider();
					baseMetadata.putAll(users.get(i).getUserMetadata());
					initManagementAPI().users().linkIdentity(primaryUserId, secondaryUserId, provider, null).execute();
				}
				/*
				 * Updating the user metadata
				 */
				var updatedUser = new com.auth0.json.mgmt.users.User();
				updatedUser.setUserMetadata(baseMetadata);
				updatedUser = initManagementAPI().users().update(primaryUserId, updatedUser).execute().getBody();
				return userMapper.toDomain(updatedUser);
			}
			return userMapper.toDomain(users.get(0));
		} catch (Auth0Exception e) {
			e.printStackTrace();
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public void assignRolesToUser(String userId, List<String> roleIds) {
		try {
			initManagementAPI().users().addRoles(userId, roleIds).execute().getBody();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public void removeRolesFromUser(String userId, List<String> roleIds) {
		try {
			initManagementAPI().users().removeRoles(userId, roleIds).execute().getBody();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public void assignUsersToRole(String roleId, List<String> userIds) {
		try {
			initManagementAPI().roles().assignUsers(roleId, userIds).execute().getBody();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public User updateUser(String id, User user) {
		try {
			com.auth0.json.mgmt.users.User updatedUser = toAuth0User(user, null, user.getPassword(),null,null);
			updatedUser = initManagementAPI().users().update(id, updatedUser).execute().getBody();
			return userMapper.toDomain(updatedUser);
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Cacheable(value = "auth0_roles")
	@Override
	public List<Role> getAllAvailableRoles() {
		try {
			return initManagementAPI().roles().list(null).execute().getBody().getItems().stream()
					.map(userMapper::toDomainRole).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Override
	public String loginWithUser(String email, String password) {
		String domain = propertyHelper.get(ConfigKey.AUTH0_BASE_URL);
		String clientId = propertyHelper.get(ConfigKey.AUTH0_MANAGEMENT_CLIENT_ID);
		String clientSecret = propertyHelper.get(ConfigKey.AUTH0_MANAGEMENT_CLIENT_SECRET);
		try {
			AuthAPI authAPI = AuthAPI.newBuilder(domain, clientId, clientSecret).build();
			TokenHolder token = authAPI.login(email, password.toCharArray()).execute().getBody();
			return token.getAccessToken();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	@Cacheable(value = "auth0_apis", key = "#identifier")
	@Override
	public List<String> getScopes(String identifier) {
		try {
			ResourceServer resourceServer = initManagementAPI().resourceServers().get(identifier).execute().getBody();
			return resourceServer.getScopes().stream().map(m -> m.getValue()).toList();
		} catch (Auth0Exception e) {
			throw new ThirdPartyException(e, ThirdPartySystem.AUTH0);
		}
	}

	private com.auth0.json.mgmt.users.User toAuth0User(User authUser, String connection, String password, Boolean isEmailVerified,
			Boolean needPasswordReset) {
		var user = new com.auth0.json.mgmt.users.User();
		user.setBlocked(authUser.getBlocked());
		user.setConnection(connection);
		user.setEmail(authUser.getEmail());
		user.setEmailVerified(isEmailVerified);
		user.setFamilyName(authUser.getLastName());
		user.setGivenName(authUser.getFirstName());
		user.setId(authUser.getUserId());
		user.setName(authUser.getFullName());
		user.setPicture(authUser.getPicture());

		Map<String, Object> userMetaData = new HashMap<>();
		if (authUser.getId() != null) {
			userMetaData.put("profile_id", authUser.getId());
		}
		userMetaData.put("active_user", true); // We are marking this always active
		if (needPasswordReset != null) {
			userMetaData.put("reset_password", needPasswordReset);//
		}
		if (authUser.getProfileCompleted() != null) {
			userMetaData.put("profile_updated", authUser.getProfileCompleted());
		}
		user.setUserMetadata(userMetaData);

		user.setPassword(password== null ? null : password.toCharArray());
		return user;

	}

}