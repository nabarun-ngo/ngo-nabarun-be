package ngo.nabarun.app.infra.core.entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.Data;

/**
 * MongoDB
 * DAO for storing expense info in DB
 */
@Document("expense")
@Data
public class ExpenseEntity {

	@Id
	private String id;
	private String expenseRefId;
	private String expenseRefType;
	private String expenseTitle;
	private String expenseDescription;
	private Double expenseAmount;
	private Date expenseCreatedOn;
	private Date expenseDate;
	private boolean deleted;
	private boolean approved;
	private String transactionRefNumber;
	private String approvedById;
	private String approvedByUserId;
	private String approvedByName;

	private String createdById;
	private String createdByUserId;
	private String createdByName;
	
	private String expenseAccountId;
	private String expenseAccountName;
	
	@ReadOnlyProperty
	@DocumentReference(lookup = "{'expenseId':?#{#self._id} }")
	private List<ExpenseItemEntity> expenses;
	
}
