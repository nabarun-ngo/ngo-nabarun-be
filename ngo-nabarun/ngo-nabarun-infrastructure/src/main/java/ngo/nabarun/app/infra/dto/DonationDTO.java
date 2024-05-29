package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.UPIOption;

@Data
public class DonationDTO {
	
	private String id;
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
	private AccountDTO paidToAccount;
	private String transactionRefNumber;
	private UserDTO donor;
	private String forEventId;
	private Boolean isPaymentNotified;
	private String comment;
	private String cancelReason;
	private String payLaterReason;
	private String paymentFailDetail;
	private Date paymentNotificationDate;
	private List<FieldDTO> additionalFields;
	private int lastPaymentDay;

	
	@Data
	public static class DonationDTOFilter{
		private String donationId;
		private List<DonationStatus> donationStatus;
		private List<DonationType> donationType;
		private Boolean isGuestDonation;
		private String donorId;
		private String donorName;
		private Date fromDate;
		private Date toDate;
		private String paidAccountId;
	}
}
