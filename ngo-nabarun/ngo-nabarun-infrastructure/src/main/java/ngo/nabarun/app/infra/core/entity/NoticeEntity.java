package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.CreatedBy;
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
	
	@CreatedBy
	private String createdBy;
	private String creatorRole;
	private Date noticeDate;
	
	@CreatedDate
	private Date createdOn;
	private Date publishedOn;
	private String visibility;
	private boolean draft;
	private int displayTill;
	private boolean deleted;
	private Boolean needMeeting;
	private MeetingEntity meetingInfo;
	private String redirectUrl;

}
