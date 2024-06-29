package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.EventType;

@Data
public class EventDetailUpdate {
	
	@JsonProperty("eventTitle")
	private String title;
	
	@JsonProperty("eventDescription")
	private String eventDescription;
	
	@JsonProperty("eventDate")
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	private Date eventDate;
	
	@JsonProperty("eventLocation")
	private String eventLocation;

	@JsonProperty("base64Image")
	private String base64Image;
	
	@JsonProperty("removeCoverPic")
	private boolean removeCoverPic;
	
	@JsonProperty("eventType")
	private EventType eventType;
	
	@JsonProperty("publish")
	private Boolean publish;
	
	@JsonProperty("eventBudget")
	private Double eventBudget;
}
