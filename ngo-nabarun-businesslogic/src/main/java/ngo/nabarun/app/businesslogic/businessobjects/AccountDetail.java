package ngo.nabarun.app.businesslogic.businessobjects;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
	
	@Data
	public static class AccountDetailFilter {
		
		@JsonProperty("status")
		private List<AccountStatus> status;
		
		@JsonProperty("type")
		private List<AccountType> type;
		
		@JsonProperty("accountHolderId")
		private String accountHolderId;
		
		@JsonProperty("includePaymentDetail")
		private boolean includePaymentDetail;
		
		@JsonProperty("includeBalance")
		private boolean includeBalance;
		
		@JsonProperty("accountId")
		private String accountId;
	}
	
	@Deprecated
	@Data
	public static class AccountDetailCreate {

		@JsonProperty("openingBalance")
		private Double openingBalance;
		
		@JsonProperty("accountHolderId")
		private String accountHolderProfileId;
		
		@JsonProperty("accountType")
		private AccountType accountType;
		
		@JsonProperty("bankDetail")
		private BankDetail bankDetail;
		
		@JsonProperty("upiDetail")
		private UPIDetail upiDetail;

	}
	
	@Deprecated
	@Data
	public static class AccountDetailUpdate {
		
		@JsonProperty("accountType")
		private AccountType accountType;
		
		@JsonProperty("bankDetail")
		private BankDetail bankDetail;
		
		@JsonProperty("upiDetail")
		private UPIDetail upiDetail;

	}
	
}
