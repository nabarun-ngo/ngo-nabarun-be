package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;
import ngo.nabarun.app.common.enums.ExpenseRefType;
import ngo.nabarun.app.common.enums.ExpenseStatus;

@Data
public class ExpenseDTO {
	private String id;
	private String name;
	private String description;
	private Date expenseDate;
	private UserDTO createdBy;
	private Date createdOn;
	private boolean admin;
	private boolean deligated;
	private UserDTO paidBy;
	private UserDTO updatedBy;
	private Date updatedOn;
	private UserDTO finalizedBy;
	private ExpenseStatus status;
	private Date finalizedOn;
	private UserDTO settledBy;
	private Date settledOn;
	private List<ExpenseItemDTO> expenseItems;
	private Double finalAmount;
	private ExpenseRefType expenseRefType;
	private String expenseRefId;
	private String txnNumber;
	private AccountDTO settlementAccount;
	private UserDTO rejectedBy;
	private Date rejectedOn;
	private String remarks;

	@Data
	public static class ExpenseDTOFilter {
		private String expId;
		private String refId;
		private ExpenseRefType refType;
		private Date startDate;
		private Date endDate;
		private String payerId;
		private List<ExpenseStatus> status;
	}

	@Data
	public static class ExpenseItemDTO {
		private String id;
		private String itemName;
		private String description;
		private Double amount;

	}

}
