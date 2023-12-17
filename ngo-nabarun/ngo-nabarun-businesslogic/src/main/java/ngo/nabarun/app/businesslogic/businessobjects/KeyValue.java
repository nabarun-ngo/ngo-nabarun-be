package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class KeyValue {
	@JsonProperty("key")
	private String key;
	
	@JsonProperty("displayValue")
	private String value;
}
