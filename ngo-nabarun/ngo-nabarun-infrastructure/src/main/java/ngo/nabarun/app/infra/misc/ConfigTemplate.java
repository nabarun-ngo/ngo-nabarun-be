package ngo.nabarun.app.infra.misc;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ConfigTemplate {

	@JsonProperty("KEY")
	private String configName;

	@JsonProperty("VALUES")
	private List<KeyValuePair> configValues;
	
	@Data
	public static class KeyValuePair {
		@JsonProperty("KEY")
		private String key;
		
		@JsonProperty("VALUE")
		private String value;
		
		@JsonProperty("DESCRIPTION")
		private String description;
		
		@JsonProperty("ATTRIBUTES")
		private Map<String,Object> attributes;

	}
	
}
