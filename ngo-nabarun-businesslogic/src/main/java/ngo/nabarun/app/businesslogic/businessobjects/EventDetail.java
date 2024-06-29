package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.EventType;

@Data
public class EventDetail {
	
	@JsonProperty("id")
	private String id;

	@JsonProperty("eventTitle")
	private String title;
	
	@JsonProperty("eventDescription")
	private String eventDescription;
	
	@JsonProperty("eventDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private Date eventDate;
	
	@JsonProperty("eventLocation")
	private String eventLocation;

	@JsonProperty("coverPicture")
	private String coverPicture;
	
	@JsonProperty("eventType")
	private EventType eventType;
	
	@JsonProperty("isDraft")
	private boolean draft;
	
	@JsonProperty("eventBudget")
	private Double eventBudget;
	
	@JsonProperty("totalExpenditure")
	private Double totalExpenditure;
	
	@JsonProperty("creatorName")
	private String creatorName;
}
