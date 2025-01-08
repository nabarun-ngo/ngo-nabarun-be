package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

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
	
	@Data
	public static class NoticeDetailFilter {
		@JsonProperty("title")
		private String title;
		
		@JsonProperty("id")
		private String id;
		
		@JsonProperty("startDate")
		private Date startDate;
		
		@JsonProperty("endDate")
		private Date endDate;
		
		@JsonProperty("status")
		private List<NoticeStatus> status;
	}

	
	@Data
	public class NoticeDetailCreate {
		
		@JsonProperty("title")
		private String title;
		
		@JsonProperty("description")
		private String description;
		
		@JsonProperty("creatorRoleCode")
		private String creatorRoleCode;
		
		@JsonProperty("noticeDate")
		private Date noticeDate;
		
		@JsonProperty("draft")
		private Boolean draft;
		
	}
	
	@Data
	public class NoticeDetailUpdate {

		@JsonProperty("title")
		private String title;
		
		@JsonProperty("description")
		private String description;
		
		@JsonProperty("creatorRoleCode")
		private String creatorRoleCode;
		
		@JsonProperty("noticeDate")
		private Date noticeDate;
		
		@JsonProperty("publish")
		private Boolean publish;
	}

	
	
}
