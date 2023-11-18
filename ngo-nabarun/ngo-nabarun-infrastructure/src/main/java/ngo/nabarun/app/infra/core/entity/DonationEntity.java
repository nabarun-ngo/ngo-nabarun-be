package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.ToString;


/**
 * @author Souvik Sarkar
 * @createdOn 27-May-2021
 * @purpose 
 */
@Document("donations")
@Data
@ToString

public class DonationEntity {
	
	@Id
	private String id;
	
	private Double amount;
	
	private Date startDate;
	
	private Date endDate;
	
	@CreatedDate
	private Date raisedOn;
	
	private String contributionType;
	
	private String contributionStatus;
	
	private Date paidOn;
	
	private String transactionRefNumber;
	
	private String paymentConfirmedBy;
	
	private Date paymentConfirmedOn;
	
	private String comment;
	
	private String guestFullNameOrOrgName;
	
	private String guestEmailAddress;
	
	private String guestContactNumber;
	
	private boolean deleted;
	
	private Date lastFollowUpOn;
	
	private String paymentMethod;
	
	private Boolean isGuest;
	
	private String accountId;
	
	private String profile;
    
   	private String eventId;

}
