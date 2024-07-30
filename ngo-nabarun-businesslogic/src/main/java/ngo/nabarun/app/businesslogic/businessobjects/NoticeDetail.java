package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.NoticeStatus;

@Data
public class NoticeDetail {
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("title")
	private String title;
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("creator")
	private UserDetail creator;
	
	@JsonProperty("creatorRoleCode")
	private String creatorRoleCode;
	
	@JsonProperty("noticeDate")
	private Date noticeDate;
	
	@JsonProperty("publishDate")
	private Date publishDate;
	
	@JsonProperty("noticeStatus")
	private NoticeStatus noticeStatus;
	
	@JsonProperty("hasMeeting")
	private boolean hasMeeting;
	
	@JsonProperty("meeting")
	private MeetingDetail meeting;
}
