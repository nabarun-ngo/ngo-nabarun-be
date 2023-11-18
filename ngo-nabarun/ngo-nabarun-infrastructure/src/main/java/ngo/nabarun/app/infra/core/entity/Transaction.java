package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * MongoDB
 * DAO for storing transactions in DB
 */
@Document("transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Transaction {

	@Id
	private String id;
	private String transactionType;
	private String status;
	private Double transactionAmt;
	
	private String transactionRef;
	
    @DocumentReference(lazy=true)
	private Account fromAccount;
    
    @DocumentReference(lazy=true)
	private Account toAcctount;
    
	private Double fromAccBalAfterTxn;
	private Double toAccBalAfterTxn;
    private Date transactionDate;
    
    @DocumentReference(lazy=true)
    private DonationEntity contribution;
    
    @DocumentReference(lazy=true)
    private Earning earning;
    
    @DocumentReference(lazy=true)
    private Expenditure expense;

}
