package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UPIDetail {
	@JsonProperty("payeeName")
	private String payeeName;
	
	@JsonProperty("upiId")
	private String upiId;
	
	@JsonProperty("mobileNumber")
	private String mobileNumber;
	
	@JsonProperty("qrData")
	private String qrData;
}
