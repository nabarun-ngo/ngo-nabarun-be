package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.MeetingRefType;
import ngo.nabarun.app.common.enums.MeetingStatus;
import ngo.nabarun.app.common.enums.MeetingType;

@Data
public class MeetingDTO {

	private String id;
	private String extMeetingId;
	private MeetingStatus status;
	private MeetingType type;
	private String summary;
	private String description;
	private String location;
	private Date startTime;
	private Date endTime;
	private List<UserDTO> attendees;
	private boolean defaultReminder;
	private List<Integer> emailReminderBeforeMin;
	private List<Integer> popupReminderBeforeMin;
	private String videoMeetingLink;
	private String audioMeetingLink;
	private String refId;
	private MeetingRefType refType;
	private String remarks;
	private List<DiscussionDTO> discussions;
	private String htmlLink;
	private String externalStatus;

	//private boolean createAuthorizationLink;
	private boolean draft;

	private String authUrl;
	private String authCallbackUrl;
	private String authState;
	private String authCode;



}
