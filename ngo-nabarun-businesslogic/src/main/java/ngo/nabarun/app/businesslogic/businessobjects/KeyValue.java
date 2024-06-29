package ngo.nabarun.app.businesslogic.businessobjects;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class KeyValue implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("key")
	private String key;
	
	@JsonProperty("displayValue")
	private String value;
}
