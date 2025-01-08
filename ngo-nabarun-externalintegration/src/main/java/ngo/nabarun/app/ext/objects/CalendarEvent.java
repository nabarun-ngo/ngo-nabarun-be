package ngo.nabarun.app.ext.objects;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.Data;
import ngo.nabarun.app.common.enums.MeetingType;

@Data
public class CalendarEvent {
	private String id;
	private String summary;
	private String description;
	private String location;
	private Date startTime;
	private Date endTime;
	private List<Map<String,String>> attendees;
	private String timeZone;
	private boolean useDefaultReminder;
	private List<Integer> emailReminderBeforeMinutes;
	private List<Integer> popupReminderBeforeMinutes;
	private String sourceId;
	private MeetingType conferenceType;
	private String conferenceLink;
	private String htmlLink;
	private String status;

	private String creatorEmail;
	
	private String action;


 
	
}
