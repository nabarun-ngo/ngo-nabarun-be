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
	private UserDTO createdBy;
	private Date createdOn;
	private Date finalizedOn;
	private UserDTO finalizedBy;
	private boolean finalized;
	private List<ExpenseItemDTO> expenseItems;
	private Double finalAmount;
	private String refId;
	private ExpenseRefType refType;
	private ExpenseStatus status;
	private String txnNumber;
	private AccountDTO account;
	private Date expenseDate;

	@Data
	public static class ExpenseDTOFilter {
		private String expId;
		private String refId;
		private ExpenseRefType refType;
		private Date startDate;
		private Date endDate;
		private Boolean approved;
		private Boolean finalized;

	}
	
	@Data
	public static class ExpenseItemDTO {
		private String id;
		private String itemName;
		private String description;
		private Double amount;
		private boolean remove;
		private AccountDTO account;
		private UserDTO createdBy;
		private Date createdOn;
		private Date date;
		private ExpenseStatus status;
		private String txnNumber;
		private UserDTO confirmedBy;


	}

}
