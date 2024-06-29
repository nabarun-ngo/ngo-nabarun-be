package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.UPIOption;

@Data@Deprecated
public class DonationDetailUpdate {
	
	@JsonProperty("amount")
	private Double amount;
	
	@JsonProperty("status")
	private DonationStatus donationStatus;
	
	@JsonProperty("paidOn")
	private Date paidOn;
	
	@JsonProperty("paymentMethod")
	private PaymentMethod paymentMethod;
		
	@JsonProperty("paidToAccount")
	private String accountId;
	
	@JsonProperty("donorName")
	private String donorName;
	
	@JsonProperty("donorEmail")
	private String donorEmail;
	
	@JsonProperty("donorMobile")
	private String donorMobile;
	
	@JsonProperty("paidUsingUPI")
	private UPIOption paidUPIName;
	
	@JsonProperty("comment")
	private String comment;
}
