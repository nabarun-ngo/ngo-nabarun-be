package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * MongoDB
 * DAO for storing expenditure info in DB
 */
@Document("expenditure")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Expenditure {

	@Id
	private String id;


	private String expenseTitle;
	
	private String expenseDescription;

	private Double expenseAmount;

	@CreatedBy
	private String expenseCreatedBy;

	@CreatedDate
	private Date expenseCreatedOn;
	
	private Date expenseDate;

	private boolean otherExpense;
	
	private boolean deleted;
	
	@DocumentReference(lazy = true)
	@JsonBackReference
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private SocialEventEntity event;
	
	private boolean approved;
	
	private String transactionRefNumber;
	
}
