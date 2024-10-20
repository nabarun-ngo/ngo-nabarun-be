package ngo.nabarun.app.businesslogic.businessobjects;

import lombok.Data;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.UPIOption;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

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
	private AccountDetail receivedAccount;
	
	@JsonProperty("donorDetails")
	private UserDetail donorDetails;
	
	@JsonProperty("forEvent")
	private EventDetail event;

	@JsonProperty("paidUsingUPI")
	private UPIOption paidUsingUPI;
	
	@JsonProperty("isPaymentNotified")
	private boolean isPaymentNotified;
	
	@JsonProperty("transactionRef")
	private String txnRef;
	
	@JsonProperty("remarks")
	private String remarks;
	
	@JsonProperty("cancelletionReason")
	private String cancelletionReason;
	
	@JsonProperty("laterPaymentReason")
	private String laterPaymentReason;
	
	@JsonProperty("paymentFailureDetail")
	private String paymentFailureDetail;

	@JsonProperty("additionalFields")
	private List<AdditionalField> additionalFields;
	
	@Data
	public static class DonationDetailFilter {
		
		@JsonProperty("isGuest")
		private Boolean isGuest;
		
		@JsonProperty("fromDate")
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private Date fromDate;
		
		@JsonProperty("toDate")
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private Date toDate;
		
		@JsonProperty("donationId")
		private String donationId;
		
		@JsonProperty("donationType")
		private List<DonationType> donationType;
		
		@JsonProperty("donationStatus")
		private List<DonationStatus> donationStatus;
		
		@JsonProperty("donorName")
		private String donorName;
		
		@JsonProperty("paidToAccountId")
		private String paidToAccountId;
		
		@JsonProperty("donorId")
		private String donorId;
		
	}
	
	@Data
	@Deprecated
	public class DonationDetailCreate {
		
		@JsonProperty("isGuest")
		private Boolean isGuest;
		
		@JsonProperty("amount")
		private Double amount;
		
		@JsonProperty("startDate")
		private Date startDate;
		
		@JsonProperty("endDate")
		private Date endDate;
		
		@JsonProperty("donationType")
		private DonationType donationType;
		
		@JsonProperty("donorId")
		private String donorId;
		
		@JsonProperty("donorName")
		private String donorName;
		
		@JsonProperty("donorEmail")
		private String donorEmail;
		
		@JsonProperty("donorMobile")
		private String donorMobile;
		
		@JsonProperty("eventId")
		private String eventId;
	}
	
	@Data
	@Deprecated
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
}
