package ngo.nabarun.app.infra.dto;

import java.util.Date;
import lombok.Data;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.UPIOption;

@Data
public class DonationDTO {
	
	private String id;
	//private String donationNumber;
	private Boolean guest;
	private Double amount;
	private Date startDate;
	private Date endDate;
	private Date raisedOn;
	private DonationType type;
	private DonationStatus status;
	private Date paidOn;
	private UserDTO confirmedBy;
	private Date confirmedOn;
	private PaymentMethod paymentMethod;
	private UPIOption upiName;
	private String accountId;
	private String transactionRefNumber;
	private UserDTO donor;
	private String forEventId;
	private Boolean isPaymentNotified;
	private String comment;
}
