package ngo.nabarun.app.infra.dto;

import java.util.Date;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import ngo.nabarun.app.common.enums.ExpenseRefType;

@Data
public class ExpenseDTO {
	private String id;
	private String name;
	private String description;
	private UserDTO createdBy;
	private Date createdOn;
	private Date expenseDate;
	private UserDTO approvedBy;
	private boolean approved;
	private AccountDTO expenseAccount;
	private List<ExpenseItemDTO> expenseItems;
	private Double finalAmount;
	private String refId;
	private ExpenseRefType refType;
	private String txnNumber;


	@Data
	public static class ExpenseDTOFilter {
		private String expId;
		private String refId;
		private ExpenseRefType refType;
		private Date startDate;
		private Date endDate;
		private Boolean approved;

	}
	@Builder
	@Data
	public static class ExpenseItemDTO {
		private String id;
		private String itemName;
		private String description;
		private Double amount;
		private boolean remove;

	}

}
