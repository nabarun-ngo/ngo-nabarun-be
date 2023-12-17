package ngo.nabarun.app.infra.dto;

import java.util.Date;


import lombok.Data;
import ngo.nabarun.app.common.enums.AccountStatus;
import ngo.nabarun.app.common.enums.AccountType;

@Data 
public class AccountDTO {
	private String id;
	//@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Double currentBalance;
	private Double openingBalance;
	private UserDTO profile;
	private AccountStatus accountStatus;
	private Date activatedOn;
	private String accountName;
	private AccountType accountType;

	private BankDTO bankDetail;
	private UPIDTO upiDetail;

}
