package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * MongoDB
 * DAO for storing notices in DB
 */
@Document("notices" )
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class NoticeEntity {
	

	@Id
	private String id;
	private String title;
	private String noticeNumber;
	private String description;
	
	private String createdById;

	private String createdBy;
	private String creatorRole;
	private Date noticeDate;
	
	@CreatedDate
	private Date createdOn;
	private Date publishedOn;
	private String visibility;
	private String status;
	private boolean draft;
	private boolean template;

	private boolean deleted;
	private Boolean needMeeting;
	
	
/**
 * meeting fields
 */
	private String meetingType;
	private String extMeetingId;
	private String meetingStatus;
	private String meetingSummary;
	private String meetingDescription;
	private String meetingLocation;
	private Date meetingDate;
	private String meetingStartTime;
	private String meetingEndTime;
	private String meetingLinkA;
	private String meetingLinkV;
	private String htmlLink;
	private String extEventStatus;
	private String creatorEmail;
	private String attendeeNames;
	private String attendeeEmails;
	private String meetingRemarks;

}
