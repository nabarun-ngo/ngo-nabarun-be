package ngo.nabarun.app.businesslogic.businessobjects;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.AccountStatus;
import ngo.nabarun.app.common.enums.AccountType;

@Data
public class AccountDetail implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("accountHolderName")
	private String accountHolderName;
		
	@JsonProperty("currentBalance")
	private Double currentBalance;
	
	@JsonProperty("accountHolder")
	private UserDetail accountHolder;
	
	@JsonProperty("accountStatus")
	private AccountStatus accountStatus;
	
	@JsonProperty("activatedOn")
	private Date activatedOn;
	
	@JsonProperty("accountType")
	private AccountType accountType;
	
	@JsonProperty("bankDetail")
	private BankDetail bankDetail;
	
	@JsonProperty("upiDetail")
	private UPIDetail upiDetail;
}
