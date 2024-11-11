package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * MongoDB
 * DAO for storing expense_items info in DB
 */
@Document("expense_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExpenseItemEntity {

	@Id
	private String id;
	private String expenseTitle;
	private String expenseDescription;
	private Double expenseAmount;
	private Date createdOn;

	private String expenseId;
	
	
	private Date expenseDate;
	private String transactionRefNumber;
	private String expenseStatus;
	
	private String paymentConfirmedById;
	private String paymentConfirmedByUserId;
	private String paymentConfirmedByName;
	
	private String createdById;
	private String createdByUserId;
	private String createdByName;
	
	
	private String expenseAccountId;
	private String expenseAccountName;
	
}
