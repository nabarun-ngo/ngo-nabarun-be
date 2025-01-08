package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;
import ngo.nabarun.app.common.enums.PaymentMethod;
import ngo.nabarun.app.common.enums.UPIOption;
import ngo.nabarun.app.common.util.CommonUtils;

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


	public Map<String, Object> toMap(Map<String, String> domainKeyValues) {
		Map<String, Object> donation = new HashMap<>();
		donation.put("id", id);
		donation.put("amount", amount);
		donation.put("donationMonth", startDate != null && endDate != null ? CommonUtils.getMonthsBetween(startDate, endDate): null);
		donation.put("startDate", startDate == null ? null :CommonUtils.formatDateToString(startDate, "dd MMM yyyy", "IST"));
		donation.put("endDate", endDate == null ? null : CommonUtils.formatDateToString(endDate, "dd MMM yyyy", "IST"));
		donation.put("raisedOn", raisedOn == null ? null :CommonUtils.formatDateToString(raisedOn, "dd MMM yyyy", "IST"));
		donation.put("type", type == null ? null : domainKeyValues.get(type.name()));
		donation.put("status", status == null ? null : domainKeyValues.get(status.name()));
		donation.put("paidOn", paidOn == null ? null :CommonUtils.formatDateToString(paidOn, "dd MMM yyyy", "IST"));
		donation.put("confirmedBy", confirmedBy.toMap(domainKeyValues));
		donation.put("confirmedOn", confirmedOn == null ? null :CommonUtils.formatDateToString(confirmedOn, "dd MMM yyyy", "IST"));
		donation.put("paymentMethod", paymentMethod == null ? null : domainKeyValues.get(paymentMethod.name()));
		donation.put("donor", donor.toMap(domainKeyValues));
		return donation;
	}


	public Map<String, Object> toHistoryMap(Map<String, String> domainKeyValues) {

		Map<String, Object> donation = new HashMap<>();
		donation.put("Id", id);
		donation.put("Guest", guest);
		donation.put("Amount", amount);
		donation.put("Start Date", startDate == null ? null :CommonUtils.formatDateToString(startDate, "dd MMM yyyy", "IST"));
		donation.put("End Date", endDate == null ? null : CommonUtils.formatDateToString(endDate, "dd MMM yyyy", "IST"));
		donation.put("Raised On", raisedOn == null ? null :CommonUtils.formatDateToString(raisedOn, "dd MMM yyyy", "IST"));
		donation.put("Type", type == null ? null : domainKeyValues.get(type.name()));
		donation.put("Status", status == null ? null : domainKeyValues.get(status.name()));
		donation.put("Paid On", paidOn == null ? null :CommonUtils.formatDateToString(paidOn, "dd MMM yyyy", "IST"));
		donation.put("Confirmed By", confirmedBy.toMap(domainKeyValues));
		donation.put("Confirmed On", confirmedOn == null ? null :CommonUtils.formatDateToString(confirmedOn, "dd MMM yyyy", "IST"));
		donation.put("Payment Method", paymentMethod == null ? null : domainKeyValues.get(paymentMethod.name()));
		donation.put("UPI Name", upiName == null ? null : domainKeyValues.get(upiName.name()));
		donation.put("Paid To Account", paidToAccount);
		donation.put("Transaction Ref Number", transactionRefNumber);
		donation.put("Event ID", forEventId);
		donation.put("Payment Notified", isPaymentNotified);
		donation.put("Payment Notification Date", paymentNotificationDate);
		donation.put("Comment", comment);
		donation.put("Pay Later Reason", payLaterReason);
		donation.put("Cancel Reason", cancelReason);
		donation.put("Payment Failure Details", paymentFailDetail);
		donation.put("Donor", donor.toMap(domainKeyValues));
		return donation;
	}

	public DonationDTO() {}
	public DonationDTO(DonationType type, double amount, Date startDate, Date endDate, UserDTO donor) {
		this.type=type;
		this.amount=amount;
		this.donor=donor;
		this.startDate=startDate;
		this.endDate=endDate;
	}
}
