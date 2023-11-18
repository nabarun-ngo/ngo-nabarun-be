package ngo.nabarun.app.ext.helpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.auth0.json.mgmt.roles.Role;
import com.auth0.json.mgmt.users.User;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;

import ngo.nabarun.app.common.enums.MeetingType;
import ngo.nabarun.app.ext.objects.AuthUser;
import ngo.nabarun.app.ext.objects.AuthUserRole;
import ngo.nabarun.app.ext.objects.CalendarEvent;

public class ObjectConverter {
	public static User toAuth0User(AuthUser authUser) {
		User user = new User();
		user.setAppMetadata(authUser.getAppMetadata());
		user.setBlocked(authUser.isBlocked());
		user.setConnection("Username-Password-Authentication");
		user.setEmail(authUser.getEmail());
		user.setEmailVerified(authUser.isEmailVerified());
		user.setFamilyName(authUser.getLastName());
		user.setGivenName(authUser.getFirstName());
		user.setId(authUser.getUserId());
		user.setName(authUser.getFullName());
		user.setPicture(authUser.getPicture());
		user.setUserMetadata(authUser.getUserMetadata());
		user.setUsername(authUser.getUsername());
		user.setVerifyEmail(authUser.isVerifyEmail());
		user.setPassword(authUser.getPassword() == null ? null : authUser.getPassword().toCharArray());
		return user;

	}

	public static AuthUser toAuthUser(User user) {
		AuthUser authUser = new AuthUser();
		authUser.setAppMetadata(user.getAppMetadata());
		authUser.setBlocked(user.isBlocked() == null ? false : user.isBlocked());
		authUser.setCreatedAt(user.getCreatedAt());
		authUser.setEmail(user.getEmail());
		authUser.setEmailVerified(user.isEmailVerified() == null ? false : user.isEmailVerified());
		authUser.setFirstName(user.getGivenName());
		authUser.setLastIp(user.getLastIP());
		authUser.setLastLogin(user.getLastLogin());
		authUser.setLastName(user.getFamilyName());
		authUser.setLastPasswordReset(user.getLastPasswordReset());
		authUser.setLoginsCount(user.getLoginsCount() == null ? 0 : user.getLoginsCount());
		authUser.setPicture(user.getPicture());
		authUser.setUpdatedAt(user.getUpdatedAt());
		authUser.setUserId(user.getId());
		authUser.setUserMetadata(user.getUserMetadata());
		authUser.setUsername(user.getUsername());
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

//		event.getEtag();
//		event.getRecurrence();
//		event.getSource();

		return calendarEvent;

	}
}
