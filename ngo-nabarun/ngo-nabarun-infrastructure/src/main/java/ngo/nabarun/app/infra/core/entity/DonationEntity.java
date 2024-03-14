package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

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
	
	private Date raisedOn;
	
	private String type;
	
	private String status;
	
	private Date paidOn;
	
	private String transactionRefNumber;
	
	private String paymentConfirmedBy;
	
	private Date paymentConfirmedOn;
	
	private String comment;
	
	private String donorName;
	
	private String donorEmailAddress;
	
	private String donorContactNumber;
	
	private boolean deleted;
	
	//private Date lastFollowUpOn;
	
	private String paymentMethod;
	
	private Boolean isGuest;
	
	private String accountId;
	private String accountName;

	private String profile;
    
   	private String eventId;
   	
	private String paidUPIName;
	private Boolean isPaymentNotified;
	private Date notifiedOn;

	//private String donationNumber;
	private String paymentConfirmedByName;


	private String cancelReason;
	private String payLaterReason;
	private String paymentFailDetail;
	
    @DocumentReference(lookup="{'source':?#{#self._id} }")
    private List<CustomFieldEntity> customFields;
	



}
