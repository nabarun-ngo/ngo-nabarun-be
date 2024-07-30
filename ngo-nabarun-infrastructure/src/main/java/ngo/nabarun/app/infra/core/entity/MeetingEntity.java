package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * MongoDB
 * DAO for storing meeting info in DB
 */
@Document("meeting_info" )
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MeetingEntity {
	@Id
	private String id;
	private String type;
	private String extMeetingId;
	private String meetingStatus;
	private String summary;
	private String description;
	private String location;
	private Date meetingDate;
	private String startTime;
	private String endTime;
	private boolean defaultReminder;
	private String emailReminderBeforeMin;
	private String popupReminderBeforeMin;
	private String meetingLinkA;
	private String meetingLinkV;
	private String htmlLink;
	private String extEventStatus;
	private String creatorEmail;
	private String attendeeNames;

	private String attendeeEmails;


	private String meetingRefId;
	private String meetingRefType;
	private String remarks;

	private String errorDetails;
	private Date errorTime;
	private String callbackUrl;
	private String authUrl;


	private boolean draft;

//	@DocumentReference(lookup = "{ 'meeting' : ?#{#self._id} }")
//	@JsonManagedReference
//	private List<MeetingAdditionalInfoEntity> additionalDetails;
	
//	@DBRef
//	private List<UserProfileEntity> attendees;
}
