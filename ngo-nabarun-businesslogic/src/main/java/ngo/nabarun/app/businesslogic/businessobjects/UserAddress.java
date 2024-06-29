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
	
	@JsonProperty("addressLine1")
	private String addressLine1;
	
	@JsonProperty("addressLine2")
	private String addressLine2;
	
	@JsonProperty("addressLine3")
	private String addressLine3;
	
	@JsonProperty("hometown")
	private String hometown;
	
	@JsonProperty("state")
	private String state;
	
	@JsonProperty("district")
	private String district;
	
	@JsonProperty("country")
	private String country;
	
//	@JsonProperty("delete")
//	private boolean delete;
}
