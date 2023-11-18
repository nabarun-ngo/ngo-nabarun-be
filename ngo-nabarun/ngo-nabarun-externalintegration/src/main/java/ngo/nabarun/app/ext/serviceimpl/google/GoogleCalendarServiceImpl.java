package ngo.nabarun.app.ext.serviceimpl.google;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.Calendar.Events;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

import ngo.nabarun.app.common.enums.MeetingType;
import ngo.nabarun.app.common.helper.GenericPropertyHelper;
import ngo.nabarun.app.ext.exception.ThirdPartyException;
import ngo.nabarun.app.ext.helpers.ObjectConverter;
import ngo.nabarun.app.ext.helpers.ThirdPartySystem;
import ngo.nabarun.app.ext.objects.CalendarEvent;
import ngo.nabarun.app.ext.service.IGoogleCalendarExtService;

@Service

public class GoogleCalendarServiceImpl implements IGoogleCalendarExtService {

	private static HttpTransport httpTransport;
	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
	private static Map<String, Credential> credentialMap = new HashMap<>();

	protected GoogleAuthorizationCodeFlow flow;
	private GenericPropertyHelper propertyHelper;

	public GoogleCalendarServiceImpl(GenericPropertyHelper propertyHelper) throws ThirdPartyException {
		this.propertyHelper = propertyHelper;
		try {
			String clientId = propertyHelper.getGoogleClientId();
			String clientSecret = propertyHelper.getGoogleClientSecret();
			Details web = new Details();
			web.setClientId(clientId);
			web.setClientSecret(clientSecret);
			GoogleClientSecrets clientSecrets = new GoogleClientSecrets().setWeb(web);
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets,
					Set.of(CalendarScopes.CALENDAR)).setApprovalPrompt("auto").build();
		} catch (GeneralSecurityException | IOException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.GOOGLE);
		}
	}

	@Override
	public String getAuthorizationUrl(String callbackUrl, String state) throws ThirdPartyException {
		return flow.newAuthorizationUrl().setRedirectUri(callbackUrl).setState(state).build();
	}

	@Override
	public boolean authorizationRequired(String id) {
		return credentialMap.get(id) != null;
	}

	@Override
	public CalendarEvent createCalendarEvent(String code, String callbackUrl, CalendarEvent calEvent)
			throws ThirdPartyException {
		try {
			Credential credential = credentialMap.get(calEvent.getId());

			if (credential == null) {
				TokenResponse response = flow.newTokenRequest(code).setRedirectUri(callbackUrl).execute();
				credential = flow.createAndStoreCredential(response, calEvent.getId());
				credentialMap.put(calEvent.getId(), credential);
			}
			Calendar client = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
					.setApplicationName(propertyHelper.getAppName()).build();
			Events events = client.events();
			Event event = new Event().setSummary(calEvent.getSummary()).setLocation(calEvent.getLocation())
					.setDescription(calEvent.getDescription());
			event.setStart(new EventDateTime().setDateTime(new DateTime(calEvent.getStartTime().getTime()))
					.setTimeZone(calEvent.getTimeZone()));

			event.setEnd(new EventDateTime().setDateTime(new DateTime(calEvent.getEndTime().getTime()))
					.setTimeZone(calEvent.getTimeZone()));

			String[] recurrence = new String[] { "RRULE:FREQ=DAILY;COUNT=1" };
			event.setRecurrence(Arrays.asList(recurrence));

			List<EventAttendee> attendees = new ArrayList<EventAttendee>();
			for (Map<String, String> attendeeMap : calEvent.getAttendees()) {
				String name = attendeeMap.get("name");
				String email = attendeeMap.get("email");
				attendees.add(new EventAttendee().setEmail(email).setDisplayName(name));
			}

			event.setAttendees(attendees);

			if (!calEvent.isUseDefaultReminder()) {
				List<EventReminder> reminder = new ArrayList<EventReminder>();

				for (Integer emailRemB4Mons : calEvent.getEmailReminderBeforeMinutes()) {
					reminder.add(new EventReminder().setMethod("email").setMinutes(emailRemB4Mons));
				}

				for (Integer popupRemB4Mons : calEvent.getPopupReminderBeforeMinutes()) {
					reminder.add(new EventReminder().setMethod("popup").setMinutes(popupRemB4Mons));
				}

				event.setReminders(new Event.Reminders().setUseDefault(false).setOverrides(reminder));
			}

			if (calEvent.getConferenceType() != MeetingType.OFFLINE) {
				ConferenceSolutionKey conferenceSKey = new ConferenceSolutionKey();
				conferenceSKey.setType(
						calEvent.getConferenceType() == MeetingType.ONLINE_VIDEO ? "hangoutsMeet" : "eventHangout");
				CreateConferenceRequest createConferenceReq = new CreateConferenceRequest();
				createConferenceReq.setRequestId(calEvent.getSourceId());
				createConferenceReq.setConferenceSolutionKey(conferenceSKey);
				ConferenceData conferenceData = new ConferenceData();
				conferenceData.setCreateRequest(createConferenceReq);
				event.setConferenceData(conferenceData);
			}

			String calendarId = "primary";
			event = calEvent.getAction().equalsIgnoreCase("create")
					? events.insert(calendarId, event).setConferenceDataVersion(1).execute()
					: events.update(calendarId, calEvent.getId(),event).setConferenceDataVersion(1).execute();
			return ObjectConverter.toCalendarEvent(event);
		} catch (IOException e) {
			throw new ThirdPartyException(e, ThirdPartySystem.GOOGLE);
		}

	}

}
