package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * MongoDB
 * DAO for storing request in DB
 */
@Document("requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowEntity{
	
	
	@Id
	private String id;
	
	private String refNumber;
	
	private boolean hidden;

	private String type;
	
	private String status;
	
	private String requestName;
	
	private String requestDescription;
	
	private String ApprovedBy1;
	
	private String ApprovedBy2;
	
	private String reasonForRejection;
	
	private String rejectedBy;
	
	private String message;
	
	@CreatedDate
	private Date createdOn;
	
	@CreatedBy
	private String createdBy;
	
	private Date resolvedOn;
	
	private String secretCode;
	
	private String requesterUserId;
	
	private RequestInfo registration;
	
	@DocumentReference
	private UserProfileEntity profile;
	
	@DocumentReference
	private UserProfileEntity delegateProfile;
	
	private Boolean delegated;
	
	private String rejoinDecision;
	
	private String getNotification;
	
	private Date rejoinDate;
	
	private Boolean continueDonation;
	
	private String suggession;
	

}
