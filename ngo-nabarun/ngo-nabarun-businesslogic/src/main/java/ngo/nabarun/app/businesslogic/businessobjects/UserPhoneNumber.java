package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.PhoneType;

@Data
public class UserPhoneNumber {
	
	@JsonProperty("ref")
	private String id;
	
	@JsonProperty("phoneType")
	private PhoneType phoneType;
	
	@JsonProperty("phoneCode")
	private String phoneCode;
	
	@JsonProperty("phoneNumber")
	private String phoneNumber;
	
	@JsonProperty("displayNumber")
	private String displayNumber;
	
	@JsonProperty("primary")
	private boolean primary;
	
	@JsonProperty("delete")
	private boolean delete;
}
