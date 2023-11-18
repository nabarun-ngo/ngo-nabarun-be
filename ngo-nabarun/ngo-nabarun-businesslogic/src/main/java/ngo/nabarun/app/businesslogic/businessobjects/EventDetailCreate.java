package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EventDetailCreate {
	
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
	
	@JsonProperty("isDraft")
	private boolean draft;
	
	@JsonProperty("eventBudget")
	private Double eventBudget;
	
}
