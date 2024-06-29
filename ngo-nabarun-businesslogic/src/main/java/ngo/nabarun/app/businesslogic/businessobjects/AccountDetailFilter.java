package ngo.nabarun.app.businesslogic.businessobjects;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import ngo.nabarun.app.common.enums.AccountStatus;
import ngo.nabarun.app.common.enums.AccountType;

@Data
public class AccountDetailFilter {
	
	@JsonProperty("status")
	private List<AccountStatus> status;
	
	@JsonProperty("type")
	private List<AccountType> type;
	
	@JsonProperty("includePaymentDetail")
	private boolean includePaymentDetail;
	
	@JsonProperty("includeBalance")
	private boolean includeBalance;
}
