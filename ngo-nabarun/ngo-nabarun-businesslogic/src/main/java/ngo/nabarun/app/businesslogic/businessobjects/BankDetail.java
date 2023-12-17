package ngo.nabarun.app.businesslogic.businessobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class BankDetail {
	
	@JsonProperty("bankAccountHolderName")
	private String bankAccountHolderName;
	
	@JsonProperty("bankName")
	private String bankName;
	
	@JsonProperty("bankBranch")
	private String bankBranch;
	
	@JsonProperty("bankAccountNumber")
	private String bankAccountNumber;
	
	@JsonProperty("bankAccountType")
	private String bankAccountType;
	
	@JsonProperty("IFSCNumber")
	private String IFSCNumber;
}
