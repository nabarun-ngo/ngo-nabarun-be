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
@Document("expense_item")
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
	private Date date;
	
	private String expenseId;
	
}
