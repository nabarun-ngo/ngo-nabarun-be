package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import lombok.Data;
/**
 * MongoDB
 * DAO for storing accounts info in DB
 */
@Document("accounts")
@Data
public class AccountEntity {

	@Id
	private String id;
	private Double currentBalance;
	private Double openingBalance;
	private String profile;
	
	private String accountStatus;
	private String accountType;
	private String accountName;
	private Date activatedOn;
	private Date createdOn;
	
	private String bankAccountHolderName;
	private String bankName;
	private String bankBranchName;
	private String bankAccountNumber;
	private String bankAccountType;
	private String bankIFSCNumber;
	
	private String upiPayeeName;
	private String upiId;
	private String upiMobileNumber;
	
	
	private String createdById;
	private String createdByName;
	private String createdByEmail;
	
	
}
