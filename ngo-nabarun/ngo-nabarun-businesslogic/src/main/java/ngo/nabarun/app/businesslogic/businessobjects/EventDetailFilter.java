package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.EventType;

@Data
public class EventDetailFilter {

	@JsonProperty("eventTitle")
	private String title;

	@JsonProperty("eventType")
	private EventType eventType;
}
