package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.NoticeStatus;

@Data
public class NoticeDetailFilter {
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
