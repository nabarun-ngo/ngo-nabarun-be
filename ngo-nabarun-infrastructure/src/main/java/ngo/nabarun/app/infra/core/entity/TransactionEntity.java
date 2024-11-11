package ngo.nabarun.app.infra.core.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

/**
 * MongoDB
 * DAO for storing transactions in DB
 */
@Document("transactions")
@Data
public class TransactionEntity {

	@Id
	private String id;
	private String transactionType;
	private String status;
	private Double transactionAmt;
	private String transactionDescription;

	private String transactionRefId;
	private String transactionRefType;

	private String fromAccount;
	private String fromAccountUserId;

	private String toAccount;
	private String toAccountUserId;

	private Double fromAccBalAfterTxn;
	private Double toAccBalAfterTxn;
    private Date transactionDate;
    private Date creationDate;
    private String comment;
    
    
	private String createdById;
	private String createdByName;
	private String createdByEmail;
	private boolean revertedTransaction;
	private String revertedById;
	private String revertedByName;
	private String revertedByEmail;

	private String revertRefTxnId;

}
