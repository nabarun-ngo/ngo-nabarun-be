package ngo.nabarun.app.infra.dto;

import java.util.Date;
import lombok.Data;
import ngo.nabarun.app.common.enums.DonationStatus;
import ngo.nabarun.app.common.enums.DonationType;

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
	private String confirmedBy;
	private Date confirmedOn;
	private String paymentMethod;
	private String accountId;
	private String transactionRefNumber;
	private UserDTO donor;
	private String forEventId;

}
