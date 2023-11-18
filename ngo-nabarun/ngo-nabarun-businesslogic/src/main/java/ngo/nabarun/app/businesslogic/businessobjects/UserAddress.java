package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.AddressType;

@Data
public class UserAddress {
	@JsonProperty("ref")
	private String id;
	
	@JsonProperty("addressType")
	private AddressType addressType;
	
	@JsonProperty("addressLine")
	private String addressLine;
	
	@JsonProperty("hometown")
	private String hometown;
	
	@JsonProperty("state")
	private String state;
	
	@JsonProperty("district")
	private String district;
	
	@JsonProperty("country")
	private String country;
	
	@JsonProperty("delete")
	private boolean delete;
}
