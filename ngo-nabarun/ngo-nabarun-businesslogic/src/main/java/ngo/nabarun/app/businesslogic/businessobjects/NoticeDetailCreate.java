package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

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
