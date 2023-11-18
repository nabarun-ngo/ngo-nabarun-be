package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.Data;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class DonationDetailFilter {
	
	@JsonProperty("isGuest")
	private Boolean isGuest;
	
	@JsonProperty("startDate")
	private Date startDate;
	
	@JsonProperty("endDate")
	private Date endDate;
	
	@JsonProperty("donationType")
	private DonationType donationType;
	
	@JsonProperty("donationStatus")
	private DonationStatus donationStatus;
	
	@JsonProperty("paymentMethod")
	private String paymentMethod;
	
	@JsonProperty("accountId")
	private String accountId;
}
