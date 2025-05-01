package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

/**
 * MongoDB
 * DAO for storing expense info in DB
 */
@Document("expenses")
@Data
public class ExpenseEntity {

	@Id
	private String id;
	private String expenseTitle;
	private String expenseDescription;
	private Date expenseDate;
	
	private Date expenseCreatedOn;
	private String createdById;
	private String createdByUserId;
	private String createdByName;
	
	private boolean deligated;
	private boolean admin;
	
	private String paidById;
	private String paidByUserId;
	private String paidByName;
	
	private Date updatedOn;
	private String updatedById;
	private String updatedByUserId;
	private String updatedByName;
	
	private Date finalizedOn;
	private String finalizedById;
	private String finalizedByUserId;
	private String finalizedByName;
	
	private String status;
	private Date settledOn;
	private String settledById;
	private String settledByUserId;
	private String settledByName;
	
	private String expenseItems;
	private Double expenseAmount;

	private String expenseRefId;
	private String expenseRefType;

	private String expenseAccountId;
	private String expenseAccountName;
	
	private String transactionRefNumber;
	private boolean deleted;
	
}
