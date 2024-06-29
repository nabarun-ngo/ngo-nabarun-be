package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.Date;

import lombok.Data;
import ngo.nabarun.app.common.enums.TransactionRefType;
import ngo.nabarun.app.common.enums.TransactionStatus;
import ngo.nabarun.app.common.enums.TransactionType;

@Data
public class TransactionDetail {
	
	private String txnId;
	private TransactionType txnType;
	private TransactionStatus txnStatus;
    private Date txnDate;
    private Double txnAmount;
    private Double accBalance;
	private String txnParticulars;
    private String txnDescription;
	
    /**
	 * Additional
	 */
	private String txnRefId;
	private TransactionRefType txnRefType;
	private AccountDetail transferFrom;
	private AccountDetail transferTo;
    private String comment;
}
