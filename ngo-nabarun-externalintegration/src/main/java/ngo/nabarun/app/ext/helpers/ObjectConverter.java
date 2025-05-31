package ngo.nabarun.app.ext.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.auth0.json.mgmt.connections.Connection;
import com.auth0.json.mgmt.resourceserver.ResourceServer;
import com.auth0.json.mgmt.resourceserver.Scope;
import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.Identity;
import com.auth0.json.mgmt.users.User;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;

import ngo.nabarun.app.common.enums.LoginMethod;
import ngo.nabarun.app.common.enums.MeetingType;
import ngo.nabarun.app.ext.objects.AuthAPIInfo;
import ngo.nabarun.app.ext.objects.AuthAPIInfo.AuthAPIScope;
import ngo.nabarun.app.ext.objects.AuthConnection;
import ngo.nabarun.app.ext.objects.AuthUser;
import ngo.nabarun.app.ext.objects.AuthUserRole;
import ngo.nabarun.app.ext.objects.CalendarEvent;

public class ObjectConverter {
	public static User toAuth0User(AuthUser authUser,String connection, String password) {
		User user = new User();
		//user.setAppMetadata(authUser.getAppMetadata());
		user.setBlocked(authUser.isBlocked());
		user.setConnection(connection);
		user.setEmail(authUser.getEmail());
		user.setEmailVerified(authUser.getEmailVerified());
		user.setFamilyName(authUser.getLastName());
		user.setGivenName(authUser.getFirstName());
		user.setId(authUser.getUserId());
		user.setName(authUser.getFullName());
		user.setPicture(authUser.getPicture());
		
		Map<String,Object> userMetaData= new HashMap<>();
		if(authUser.getProfileId() != null) {
			userMetaData.put("profile_id", authUser.getProfileId());
		}
		userMetaData.put("active_user", !authUser.isInactive());
		if(authUser.getResetPassword() != null) {
			userMetaData.put("reset_password", authUser.getResetPassword());
		}
		if(authUser.getProfileUpdated() != null) {
			userMetaData.put("profile_updated", authUser.getProfileUpdated());
		}
		user.setUserMetadata(userMetaData);
		user.setUsername(authUser.getUsername());
		user.setVerifyEmail(authUser.getVerifyEmail());
		
		user.setPassword(password == null ? null : password.toCharArray());
		return user;

	}

	public static AuthUser toAuthUser(User user) {
		//System.err.println(user);
		AuthUser authUser = new AuthUser();
		//authUser.setAppMetadata(user.getAppMetadata());
		authUser.setBlocked(user.isBlocked() == null ? false : user.isBlocked());
		authUser.setCreatedAt(user.getCreatedAt());
		authUser.setEmail(user.getEmail());
		authUser.setEmailVerified(user.isEmailVerified() == null ? false : user.isEmailVerified());
		authUser.setFirstName(user.getGivenName());
		authUser.setLastIp(user.getLastIP());
		authUser.setLastLogin(user.getLastLogin());
		authUser.setLastName(user.getFamilyName());
		authUser.setFullName(user.getName());
		authUser.setLastPasswordReset(user.getLastPasswordReset());
		authUser.setLoginsCount(user.getLoginsCount() == null ? 0 : user.getLoginsCount());
		authUser.setPicture(user.getPicture());
		authUser.setUpdatedAt(user.getUpdatedAt());
		authUser.setUserId(user.getId());
		//authUser.setUserMetadata(user.getUserMetadata());
		
		if(user != null && user.getIdentities() != null) {
			List<Identity> identities=user.getIdentities();
			List<LoginMethod> loginMethods= new ArrayList<>();
			for(Identity identity:identities) {
				//System.err.println(provider);
				switch(identity.getProvider()) {
				case "auth0":
					if("Username-Password-Authentication".equalsIgnoreCase(identity.getConnection())) {
						loginMethods.add(LoginMethod.PASSWORD);
					}
					break;
				case "email":
					loginMethods.add(LoginMethod.EMAIL);
					break;
				case "sms":
					loginMethods.add(LoginMethod.SMS);
					break;
				}
			}
			authUser.setProviders(loginMethods);
		}
		authUser.setUsername(user.getUsername());
		//System.err.println(user.getUserMetadata());
		if(user != null && user.getUserMetadata() != null) {
			Object profileId=user.getUserMetadata().get("profile_id");
			authUser.setProfileId(profileId == null ? null : profileId.toString());	
			Object active_user=user.getUserMetadata().get("active_user");
			authUser.setInactive(active_user == null ? false : !Boolean.valueOf(active_user.toString()));
			Object reset_password=user.getUserMetadata().get("reset_password");
			authUser.setResetPassword(reset_password == null ? false : !Boolean.valueOf(reset_password.toString()));
		}

		return authUser;

	}

	public static AuthUserRole toAuthUserRole(Role role) {
		AuthUserRole authUserRole = new AuthUserRole();
		authUserRole.setRoleId(role.getId());
		authUserRole.setRoleDescription(role.getDescription());
		authUserRole.setRoleName(role.getName());
		return authUserRole;

	}

	public static CalendarEvent toCalendarEvent(Event event) {
		CalendarEvent calendarEvent = new CalendarEvent();
		List<String> attendees = new ArrayList<>();
		for (EventAttendee attendee : event.getAttendees()) {
			attendees.add(attendee.getDisplayName() + "|" + attendee.getEmail());
		}
		calendarEvent.setDescription(event.getDescription());
		if (event.getConferenceData() != null && event.getConferenceData().getCreateRequest() != null) {
			calendarEvent.setSourceId(event.getConferenceData().getCreateRequest().getRequestId());
		}
		calendarEvent.setDescription(event.getDescription());
		if(event.getReminders() != null && event.getReminders().getUseDefault() == Boolean.FALSE ) {
			calendarEvent.setEmailReminderBeforeMinutes(event.getReminders().getOverrides().stream().filter(f->"email".equalsIgnoreCase(f.getMethod())) .map(m->m.getMinutes()).toList());
			calendarEvent.setPopupReminderBeforeMinutes(event.getReminders().getOverrides().stream().filter(f->"popup".equalsIgnoreCase(f.getMethod())) .map(m->m.getMinutes()).toList());
		}		
		calendarEvent.setEndTime(new Date(event.getEnd().getDateTime().getValue()));
		calendarEvent.setId(event.getId());
		calendarEvent.setLocation(event.getLocation());
		calendarEvent.setStartTime(new Date(event.getStart().getDateTime().getValue()));
		calendarEvent.setSummary(event.getSummary());
		calendarEvent.setTimeZone(event.getStart().getTimeZone());
		calendarEvent.setStatus(event.getStatus());
		calendarEvent.setUseDefaultReminder(event.getReminders().getUseDefault());
		calendarEvent.setConferenceLink(event.getHangoutLink());
		calendarEvent.setHtmlLink(event.getHtmlLink());
		if (event.getConferenceData() != null) {
			calendarEvent.setConferenceType("hangoutsMeet".equalsIgnoreCase(
					event.getConferenceData().getConferenceSolution().getKey().getType()) ? MeetingType.ONLINE_VIDEO
							: MeetingType.ONLINE_AUDIO);
		}
		calendarEvent.setCreatorEmail(event.getCreator() == null ? null : event.getCreator().getEmail());
		return calendarEvent;

	}

	public static AuthConnection toAuthConnection(Connection connection) {
		String passwordPolicy = null;
		System.err.println(connection.getOptions());
		if(connection.getOptions() != null && connection.getOptions().containsKey("passwordPolicy")) {
			passwordPolicy =  String.valueOf(connection.getOptions().get("passwordPolicy"));
		}
		AuthConnection aconn= new AuthConnection();
		aconn.setId(connection.getId());
		aconn.setName(connection.getName());
		aconn.setPasswordPolicy(passwordPolicy);
		aconn.setStrategy(connection.getStrategy());
		aconn.setDatabaseConnection(connection.getName().equalsIgnoreCase("Username-Password-Authentication"));
		return aconn;
	}

	public static AuthAPIInfo toAuthAPIInfo(ResourceServer resourceServer) {
		AuthAPIInfo authAPIInfo= new AuthAPIInfo();
		authAPIInfo.setId(resourceServer.getId());
		authAPIInfo.setIdentifier(resourceServer.getIdentifier());
		authAPIInfo.setName(resourceServer.getName());
		List<AuthAPIScope> scopeList= new ArrayList<>();
		for(Scope scope:resourceServer.getScopes()) {
			AuthAPIScope authAPIScope= new AuthAPIScope();
			authAPIScope.setDescription(scope.getDescription());
			authAPIScope.setValue(scope.getValue());
			scopeList.add(authAPIScope);
		}
		authAPIInfo.setScopes(scopeList);
		return authAPIInfo;
	}
	
	
}
