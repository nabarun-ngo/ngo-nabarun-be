package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class NoticeDetailFilter {
	@JsonProperty("title")
	private String title;
	
	@JsonProperty("noticeNumber")
	private String noticeNumber;
}
