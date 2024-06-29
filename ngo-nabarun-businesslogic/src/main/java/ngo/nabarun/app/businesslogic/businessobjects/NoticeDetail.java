package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NoticeDetail {
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("title")
	private String title;
	
	@JsonProperty("noticeNumber")
	private String noticeNumber;
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("creatorName")
	private String creatorName;
	
	@JsonProperty("creatorRole")
	private String creatorRole;
	
	@JsonProperty("creatorRoleCode")
	private String creatorRoleCode;
	
	@JsonProperty("noticeDate")
	private Date noticeDate;
	
	@JsonProperty("publishDate")
	private Date publishDate;
	
	@JsonProperty("meeting")
	private MeetingDetail meeting;
}
