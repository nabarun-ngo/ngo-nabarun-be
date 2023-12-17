package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.Data;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.UPIOption;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class DonationDetail {
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("isGuest")
	private Boolean isGuest;
	
	@JsonProperty("amount")
	private Double amount;
	
	@JsonProperty("startDate")
	private Date startDate;
	
	@JsonProperty("endDate")
	private Date endDate;
	
	@JsonProperty("raisedOn")
	private Date raisedOn;
	
	@JsonProperty("type")
	private DonationType donationType;
	
	@JsonProperty("status")
	private DonationStatus donationStatus;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@JsonProperty("paidOn")
	private Date paidOn;
	
	@JsonProperty("confirmedBy")
	private UserDetail paymentConfirmedBy;
	
	@JsonProperty("confirmedOn")
	private Date paymentConfirmedOn;
	
	@JsonProperty("paymentMethod")
	private PaymentMethod paymentMethod;
	
	@JsonProperty("paidToAccount")
	private String accountId;
	
	@JsonProperty("donorDetails")
	private UserDetail donorDetails;
	
	@JsonProperty("forEvent")
	private EventDetail event;
	    
	@JsonProperty("documents")
	private List<String> attachments;
	
	@JsonProperty("paidUsingUPI")
	private UPIOption paidUsingUPI;
	
	@JsonProperty("isPaymentNotified")
	private boolean isPaymentNotified;
	
	@JsonProperty("donationRef")
	private String donationRef;
	
	
}
