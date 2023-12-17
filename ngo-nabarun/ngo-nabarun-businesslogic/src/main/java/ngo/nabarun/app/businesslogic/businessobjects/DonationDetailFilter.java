package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.Data;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.PaymentMethod;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class DonationDetailFilter {
	
	@JsonProperty("isGuest")
	private Boolean isGuest;
	
	@JsonProperty("fromDate")
	private Date fromDate;
	
	@JsonProperty("toDate")
	private Date toDate;
	
	@JsonProperty("donationType")
	private DonationType donationType;
	
	@JsonProperty("donationStatus")
	private DonationStatus donationStatus;
	
	@JsonProperty("paymentMethod")
	private PaymentMethod paymentMethod;
	
}
