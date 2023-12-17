package ngo.nabarun.app.infra.dto;

import java.util.Date;

import lombok.Data;
import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.common.enums.TransactionStatus;
import ngo.nabarun.app.common.enums.TransactionType;


@Data
public class TransactionDTO {
	
	private String id;
	private String txnDescription;
	private TransactionType txnType;
	private TransactionStatus txnStatus;
	private Double txnAmount;
	private String txnRefId;
	private TransactionRefType txnRefType;
	private AccountDTO fromAccount;
	private AccountDTO toAccount;
	private Double fromAccBalAfterTxn;
	private Double toAccBalAfterTxn;
    private Date txnDate;
    private String comment;


}
