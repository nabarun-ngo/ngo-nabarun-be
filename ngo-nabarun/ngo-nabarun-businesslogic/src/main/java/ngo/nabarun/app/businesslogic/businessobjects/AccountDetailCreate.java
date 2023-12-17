package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.AccountType;

@Data
public class AccountDetailCreate {

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
