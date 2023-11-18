package ngo.nabarun.app.infra.misc;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class KeyValuePair {
	@JsonProperty("KEY")
	private String key;
	
	@JsonProperty("DISPLAY_VALUE")
	private String displayValue;
	
	@JsonProperty("DESCRIPTION")
	private String description;
	
	@JsonProperty("ATTRIBUTES")
	private Map<String,Object> attributes;

}
