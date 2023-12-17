package ngo.nabarun.app.infra.dto;

import lombok.Data;

@Data
public class BankDTO {
	private String accountHolderName;
	private String bankName;
	private String branchName;
	private String accountNumber;
	private String accountType;
	private String IFSCNumber;
}
